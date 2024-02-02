/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.map;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorld.Restriction;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.loungebridge.util.game.ResetableMap;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.library.basic.util.Loggers;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PushMap extends Map implements ResetableMap {

  private static final int ZOMBIE_SPAWN_INDEX = 0;
  private static final int BLUE_SPAWN = 10;
  private static final int RED_SPAWN = 20;

  private static final int ITEM_SPAWNERS = 100;

  // x0 -> villager finish
  // x[1-10] -> team spawns

  private static final List<Vector> STRAIGHT = List.of(new Vector(-1, 0, 0), new Vector(1, 0, 0),
      new Vector(0, 0, -1), new Vector(0, 0, 1));
  private static final List<Vector> DIAGONAL = List.of(new Vector(-1, 0, -1),
      new Vector(-1, 0, 1), new Vector(1, 0, -1), new Vector(1, 0, 1), new Vector(-1, -1, 0),
      new Vector(-1, 1, 0), new Vector(1, -1, 0), new Vector(1, 1, 0), new Vector(0, -1, -1),
      new Vector(0, -1, 1), new Vector(0, 1, -1), new Vector(0, 1, 1));
  private static final List<Vector> TETRAGONAL = List.of(new Vector(-1, -1, -1),
      new Vector(-1, -1, 1), new Vector(-1, 1, -1), new Vector(-1, 1, 1), new Vector(1, -1, -1),
      new Vector(1, -1, 1), new Vector(1, 1, -1), new Vector(1, 1, 1));

  private final List<Material> villagerPathMaterials = new LinkedList<>();
  private final int laps;

  private PathPoint spawnPoint;
  private final List<ItemSpawner> itemSpawners = new LinkedList<>();

  private final LinkedList<Integer> blueSpawnIndizes = new LinkedList<>();
  private final LinkedList<Integer> redSpawnIndizes = new LinkedList<>();

  public PushMap(DbMap map, boolean loadWorld) {
    super(map, loadWorld);

    ExWorld world = this.getWorld();
    if (world != null) {
      world.restrict(Restriction.BLOCK_PLACE, true);
      world.restrict(Restriction.FIRE_SPREAD_SPEED, 0f);
      world.restrict(Restriction.BLOCK_BREAK, true);
      world.restrict(Restriction.ENTITY_BLOCK_BREAK, true);
      world.restrict(Restriction.ENTITY_EXPLODE, false);
      world.restrict(Restriction.BLOCK_BURN_UP, true);
      world.restrict(Restriction.LIGHT_UP_INTERACTION, false);
      world.restrict(Restriction.FLUID_COLLECT, true);
      world.restrict(Restriction.FLUID_PLACE, true);
      world.restrict(Restriction.FLINT_AND_STEEL, true);
      world.restrict(Restriction.OPEN_INVENTORIES, List.of(Material.AIR));
      world.setExceptService(true);
      world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
      world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
      world.setGameRule(GameRule.DO_FIRE_TICK, false);
      world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
      world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
      world.setTime(1000);
      world.setStorm(false);
      world.setAutoSave(false);
      world.setPVP(true);
    }

    this.laps = this.getProperty("laps", Integer.class, 3,
        v -> Loggers.GAME.warning("Can not load laps of map " + super.name + ", laps is not an integer"));

    String[] materialNames = super.getProperty("materials").split(",");

    for (String materialName : materialNames) {
      Material material = Material.getMaterial(materialName.toUpperCase());

      if (material == null) {
        Loggers.GAME.warning("Unknown material for map " + this.getName() + ": " + materialName);
      } else {
        this.villagerPathMaterials.add(material);
      }
    }

    if (this.villagerPathMaterials.isEmpty()) {
      Loggers.GAME.warning("Unknown material for map " + this.getName() + ": " + String.join(", ", materialNames));
      this.villagerPathMaterials.add(Material.BEDROCK);
    }

    if (this.getZombieSpawn() == null) {
      Loggers.GAME.warning("Missing zombie location in map " + this.getName());
      return;
    }

    this.spawnPoint = new PathPoint(this.getZombieSpawn().clone().middleHorizontalBlock());

    for (int i = 0; i < 10; i++) {
      int blueIndex = BLUE_SPAWN + i;
      int redIndex = RED_SPAWN + i;

      if (super.containsLocation(blueIndex)) {
        this.blueSpawnIndizes.add(blueIndex);
      }

      if (super.containsLocation(redIndex)) {
        this.redSpawnIndizes.add(redIndex);
      }
    }

    // villager path

    PathPoint firstToBlue = null;
    PathPoint firstToRed = null;

    for (List<Vector> vecs : List.of(STRAIGHT, DIAGONAL, TETRAGONAL)) {
      for (Vector vec : vecs) {
        ExLocation nearLoc = this.spawnPoint.getLocation().clone().add(vec).middleHorizontalBlock();
        Material type = nearLoc.clone().add(0, -1, 0).getBlock().getType();

        if (type.equals(Material.BLUE_WOOL)) {
          firstToBlue = new PathPoint(nearLoc);
        } else if (type.equals(Material.RED_WOOL)) {
          firstToRed = new PathPoint(nearLoc);
        }
      }
    }

    if (firstToBlue == null || firstToRed == null) {
      Loggers.GAME.warning("No directions to blue/red found " + this.getName());
      firstToBlue = new PathPoint(this.getNextLocation(this.spawnPoint.getLocation()));
      this.spawnPoint.setNextToBlue(firstToBlue);
      firstToRed = new PathPoint(this.getNextLocation(this.spawnPoint.getLocation()));
      this.spawnPoint.setNextToRed(firstToRed);
    } else {
      this.spawnPoint.setNextToBlue(firstToBlue);
      this.spawnPoint.setNextToRed(firstToRed);
    }

    PathPoint previous = firstToBlue;

    ExLocation current = getNextLocation(firstToBlue.getLocation());

    while (current != null) {
      previous = previous.setNextToBlue(new PathPoint(current.middleHorizontalBlock()));
      current = getNextLocation(current);
    }

    previous = firstToRed;
    current = getNextLocation(firstToRed.getLocation());

    while (current != null) {
      previous = previous.setNextToRed(new PathPoint(current.middleHorizontalBlock()));
      current = getNextLocation(current);
    }

    for (ExLocation location : super.getLocations(ITEM_SPAWNERS)) {
      this.itemSpawners.add(new ItemSpawner(location));
    }
  }

  private ExLocation getNextLocation(ExLocation current) {
    for (List<Vector> vecs : List.of(STRAIGHT, DIAGONAL, TETRAGONAL)) {
      for (Vector vec : vecs) {
        ExLocation nearLoc = current.clone().add(vec).middleHorizontalBlock();
        if (!this.spawnPoint.contains(nearLoc) && this.villagerPathMaterials.contains(
            nearLoc.clone().add(0, -1, 0).getBlock().getType())) {
          return nearLoc;
        }
      }
    }
    return null;
  }

  public int getLaps() {
    return laps;
  }

  public ExLocation getZombieSpawn() {
    return super.getLocation(ZOMBIE_SPAWN_INDEX);
  }

  public ExLocation getBlueFinish() {
    return super.getLocation(BLUE_SPAWN);
  }

  public ExLocation getRedFinish() {
    return super.getLocation(RED_SPAWN);
  }

  public ExLocation getRandomBlueSpawn() {
    return super.getLocation(this.blueSpawnIndizes.get(new Random().nextInt(this.blueSpawnIndizes.size())));
  }

  public Collection<ExLocation> getBlueSpawns() {
    return super.getLocations(this.blueSpawnIndizes);
  }

  public ExLocation getRandomRedSpawn() {
    return super.getLocation(this.redSpawnIndizes.get(new Random().nextInt(this.redSpawnIndizes.size())));
  }

  public Collection<ExLocation> getRedSpawns() {
    return super.getLocations(this.redSpawnIndizes);
  }

  public PathPoint getSpawnPoint() {
    return spawnPoint;
  }

  public List<ItemSpawner> getItemSpawners() {
    return this.itemSpawners;
  }
}
