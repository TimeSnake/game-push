/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.push.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.loungebridge.util.game.ResetableMap;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.game.push.main.Plugin;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PushMap extends Map implements ResetableMap {

    private static final int VILLAGER_SPAWN_INDEX = 0;
    private static final int BLUE_SPAWN = 10;
    private static final int RED_SPAWN = 20;

    private static final int ITEM_SPAWNERS = 100;

    // x0 -> villager finish
    // x[0-10] -> team spawns

    private static final List<Vector> STRAIGHT = List.of(new Vector(-1, 0, 0), new Vector(1, 0, 0), new Vector(0, 0,
            -1), new Vector(0, 0, 1));
    private static final List<Vector> DIAGONAL = List.of(new Vector(-1, 0, -1), new Vector(-1, 0, 1), new Vector(1, 0
                    , -1), new Vector(1, 0, 1), new Vector(-1, -1, 0), new Vector(-1, 1, 0), new Vector(1, -1, 0),
            new Vector(1, 1, 0), new Vector(0, -1, -1), new Vector(0, -1, 1), new Vector(0, 1, -1), new Vector(0, 1,
                    1));
    private static final List<Vector> TETRAGONAL = List.of(new Vector(-1, -1, -1), new Vector(-1, -1, 1),
            new Vector(-1, 1, -1), new Vector(-1, 1, 1), new Vector(1, -1, -1), new Vector(1, -1, 1), new Vector(1, 1
                    , -1), new Vector(1, 1, 1));

    private final List<Material> villagerPathMaterials = new LinkedList<>();
    private final int laps;

    private final PathPoint spawnPoint;
    private final List<ItemSpawner> itemSpawners = new LinkedList<>();

    private final LinkedList<Integer> blueSpawnIndizes = new LinkedList<>();
    private final LinkedList<Integer> redSpawnIndizes = new LinkedList<>();

    public PushMap(DbMap map, boolean loadWorld) {
        super(map, loadWorld);

        ExWorld world = this.getWorld();
        if (world != null) {
            Server.getWorldManager().backupWorld(world);
            world.restrict(ExWorld.Restriction.BLOCK_PLACE, true);
            world.restrict(ExWorld.Restriction.FIRE_SPREAD, true);
            world.restrict(ExWorld.Restriction.BLOCK_BREAK, true);
            world.restrict(ExWorld.Restriction.ENTITY_BLOCK_BREAK, true);
            world.restrict(ExWorld.Restriction.ENTITY_EXPLODE, false);
            world.restrict(ExWorld.Restriction.BLOCK_BURN_UP, true);
            world.restrict(ExWorld.Restriction.LIGHT_UP_INTERACTION, false);
            world.restrict(ExWorld.Restriction.FLUID_COLLECT, true);
            world.restrict(ExWorld.Restriction.FLUID_PLACE, true);
            world.restrict(ExWorld.Restriction.FLINT_AND_STEEL, true);
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

        this.laps = Integer.parseInt(super.getInfo().get(0));

        String[] materialNames = super.getInfo().get(1).split(",");

        for (String materialName : materialNames) {
            Material material = Material.getMaterial(materialName.toUpperCase());

            if (material == null) {
                Server.printWarning(Plugin.PUSH,
                        "Unknown material for map " + this.getName() + ": " + materialName, "Map");
            } else {
                this.villagerPathMaterials.add(material);
            }
        }

        if (this.villagerPathMaterials.isEmpty()) {
            Server.printWarning(Plugin.PUSH,
                    "Unknown material for map " + this.getName() + ": " + super.getInfo().get(0), "Map");
            this.villagerPathMaterials.add(Material.BEDROCK);
        }

        this.spawnPoint = new PathPoint(this.getZombieSpawn().clone().middleBlock());

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
                ExLocation nearLoc = this.spawnPoint.getLocation().clone().add(vec).middleBlock();
                Material type = nearLoc.clone().add(0, -1, 0).getBlock().getType();

                if (type.equals(Material.BLUE_WOOL)) {
                    firstToBlue = new PathPoint(nearLoc);
                } else if (type.equals(Material.RED_WOOL)) {
                    firstToRed = new PathPoint(nearLoc);
                }
            }
        }

        if (firstToBlue == null || firstToRed == null) {
            Server.printWarning(Plugin.PUSH,
                    "No directions to blue/red found " + this.getName() + ": " + super.getInfo().get(0), "Map");
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
            previous = previous.setNextToBlue(new PathPoint(current.middleBlock()));

            current = getNextLocation(current);
        }

        previous = firstToRed;

        current = getNextLocation(firstToRed.getLocation());

        while (current != null) {
            previous = previous.setNextToRed(new PathPoint(current.middleBlock()));

            current = getNextLocation(current);
        }

        for (ExLocation location : super.getLocations(ITEM_SPAWNERS)) {
            this.itemSpawners.add(new ItemSpawner(location));
        }
    }

    private ExLocation getNextLocation(ExLocation current) {
        for (List<Vector> vecs : List.of(STRAIGHT, DIAGONAL, TETRAGONAL)) {
            for (Vector vec : vecs) {
                ExLocation nearLoc = current.clone().add(vec).middleBlock();
                if (!this.spawnPoint.contains(nearLoc) && this.villagerPathMaterials.contains(nearLoc.clone().add(0,
                        -1, 0).getBlock().getType())) {
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
        return super.getLocation(VILLAGER_SPAWN_INDEX);
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

    public ExLocation getRandomRedSpawn() {
        return super.getLocation(this.redSpawnIndizes.get(new Random().nextInt(this.redSpawnIndizes.size())));
    }

    public PathPoint getSpawnPoint() {
        return spawnPoint;
    }

    public List<ItemSpawner> getItemSpawners() {
        return this.itemSpawners;
    }
}
