package de.timesnake.game.push.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.game.util.Map;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.game.push.main.Plugin;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PushMap extends Map {

    private static final int VILLAGER_SPAWN_INDEX = 0;
    private static final int BLUE_FINISH = 10;
    private static final int RED_FINISH = 20;

    // x0 -> villager finish
    // x[1-9] -> team spawns

    private static final List<Vector> STRAIGHT = List.of(new Vector(-1, 0, 0), new Vector(1, 0, 0), new Vector(0, 0, -1), new Vector(0, 0, 1));
    private static final List<Vector> DIAGONAL = List.of(new Vector(-1, 0, -1), new Vector(-1, 0, 1), new Vector(1, 0, -1), new Vector(1, 0, 1), new Vector(-1, -1, 0), new Vector(-1, 1, 0), new Vector(1, -1, 0), new Vector(1, 1, 0), new Vector(0, -1, -1), new Vector(0, -1, 1), new Vector(0, 1, -1), new Vector(0, 1, 1));
    private static final List<Vector> TETRAGONAL = List.of(new Vector(-1, -1, -1), new Vector(-1, -1, 1), new Vector(-1, 1, -1), new Vector(-1, 1, 1), new Vector(1, -1, -1), new Vector(1, -1, 1), new Vector(1, 1, -1), new Vector(1, 1, 1));

    private final Material villagerPathMaterial;
    private final int laps;

    private final PathPoint spawnPoint;

    private final LinkedList<Integer> blueSpawnIndizes = new LinkedList<>();
    private final LinkedList<Integer> redSpawnIndizes = new LinkedList<>();

    public PushMap(DbMap map, boolean loadWorld) {
        super(map, loadWorld);

        ExWorld world = this.getWorld();
        if (world != null) {
            Server.getWorldManager().backupWorld(world);
            world.allowBlockPlace(false);
            world.allowFireSpread(false);
            world.allowBlockBreak(false);
            world.allowEntityExplode(false);
            world.allowBlockBurnUp(false);
            world.allowLightUpInteraction(true);
            world.allowFluidCollect(false);
            world.allowFluidPlace(false);
            world.allowFlintAndSteel(true);
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

        Material material = Material.getMaterial(super.getInfo().get(1));

        if (material == null) {
            Server.printWarning(Plugin.PUSH, "Unknown material for map " + this.getName() + ": " + super.getInfo().get(0), "Map");
            material = Material.BEDROCK;
        }
        this.villagerPathMaterial = material;

        this.spawnPoint = new PathPoint(this.getZombieSpawn().clone().middleBlock());

        for (int i = 1; i < 9; i++) {
            int blueIndex = BLUE_FINISH + i;
            int redIndex = RED_FINISH + i;

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
            //System.out.println("blue " + current.getX() + " " + current.getY() + " " + current.getZ());

            current = getNextLocation(current);
        }

        previous = firstToRed;

        current = getNextLocation(firstToRed.getLocation());

        while (current != null) {
            previous = previous.setNextToRed(new PathPoint(current.middleBlock()));
            //System.out.println("red " + current.getX() + " " + current.getY() + " " + current.getZ());

            current = getNextLocation(current);
        }
    }

    private ExLocation getNextLocation(ExLocation current) {
        for (List<Vector> vecs : List.of(STRAIGHT, DIAGONAL, TETRAGONAL)) {
            for (Vector vec : vecs) {
                ExLocation nearLoc = current.clone().add(vec).middleBlock();
                if (!this.spawnPoint.contains(nearLoc) && nearLoc.clone().add(0, -1, 0).getBlock().getType().equals(this.villagerPathMaterial)) {
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
        return super.getLocation(BLUE_FINISH);
    }

    public ExLocation getRedFinish() {
        return super.getLocation(RED_FINISH);
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
}
