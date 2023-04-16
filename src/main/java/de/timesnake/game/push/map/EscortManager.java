/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.server.PushServer;
import de.timesnake.game.push.user.PushUser;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.bukkit.ExZombie;
import de.timesnake.library.entities.pathfinder.custom.CustomPathfinderGoalUpdatedLocation;
import de.timesnake.library.entities.pathfinder.custom.ExCustomPathfinderGoalUpdatedLocation;
import de.timesnake.library.entities.wrapper.ExEnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitTask;

public class EscortManager {

    private static final double RADIUS = 5;
    private static final double DEFAULT_SPEED = 1.1;
    private static final double MAX_SPEED = 1.7;
    private static final double SPEED_INCREASE = 30;
    private static final int STOP_PLAYER_NUMBER = 2;

    private ExZombie zombie;
    private PathPoint currentPathPoint;
    private boolean lastDirectionBlue;

    private BukkitTask moveTask;

    private CustomPathfinderGoalUpdatedLocation pathfinder;

    private int time;
    private double baseSpeed;

    public EscortManager() {

    }

    public ExZombie getZombie() {
        return zombie;
    }

    public void start() {
        for (Zombie zombie : PushServer.getMap().getWorld().getEntitiesByClass(Zombie.class)) {
            zombie.remove();
        }

        this.time = 0;
        this.baseSpeed = DEFAULT_SPEED;

        this.spawn();
        this.runMoveTask();
    }

    public void stop() {
        if (this.moveTask != null) {
            this.moveTask.cancel();
        }

        if (this.zombie != null) {
            this.zombie.remove();
        }
    }

    public void spawn() {
        PushMap map = PushServer.getMap();

        this.currentPathPoint = map.getSpawnPoint();

        if (this.zombie != null) {
            this.zombie.remove();
        }

        this.zombie = new ExZombie(map.getWorld().getBukkitWorld(), false, false);

        this.zombie.setInvulnerable(true);
        this.zombie.setPersistent(true);
        this.zombie.setRemoveWhenFarAway(false);

        this.zombie.setCustomName("Zombie");
        this.zombie.setCustomNameVisible(false);

        this.zombie.setSlot(ExEnumItemSlot.HEAD,
                new ExItemStack(Material.GOLDEN_HELMET).setUnbreakable(true));

        this.pathfinder = new CustomPathfinderGoalUpdatedLocation(null, this.baseSpeed, 32, 0.2) {
            @Override
            public Location getNextLocation(Location entityLoc) {
                return EscortManager.this.currentPathPoint.getLocation();
            }
        };

        this.zombie.addPathfinderGoal(1,
                new ExCustomPathfinderGoalUpdatedLocation(this.pathfinder));

        this.zombie.setCollidable(false);

        this.zombie.setMaxHealth(2048);
        this.zombie.setHealth(2048);

        ExLocation spawn = map.getZombieSpawn();
        this.zombie.setPosition(spawn.getX(), spawn.getY(), spawn.getZ());

        EntityManager.spawnEntity(map.getWorld().getBukkitWorld(), this.zombie);

        Loggers.GAME.info("Spawned zombie");
    }

    public void runMoveTask() {
        this.moveTask = Server.runTaskTimerSynchrony(() -> {
            int countBlue = 0;
            int countRed = 0;

            for (Player player : this.zombie.getLocation().getNearbyPlayers(RADIUS)) {
                PushUser user = (PushUser) Server.getUser(player);
                if (user.getTeam() == null || !user.getStatus().equals(Status.User.IN_GAME)) {
                    continue;
                }

                if (user.getTeam().equals(PushServer.getGame().getBlueTeam())) {
                    countBlue++;
                } else if (user.getTeam().equals(PushServer.getGame().getRedTeam())) {
                    countRed++;
                }
            }

            if (countBlue > countRed) {
                double speed = this.baseSpeed - (this.baseSpeed / STOP_PLAYER_NUMBER * countRed);
                if (speed > 0) {
                    this.moveTo(false, speed);
                }
            } else if (countRed > countBlue) {
                double speed = this.baseSpeed - (this.baseSpeed / STOP_PLAYER_NUMBER * countBlue);
                if (speed > 0) {
                    this.moveTo(true, speed);
                }
            }

            if (time % SPEED_INCREASE * 2 == 0 && this.baseSpeed < MAX_SPEED) {
                this.baseSpeed += 0.05;
            }

            time++;

        }, 0, 10, GamePush.getPlugin());
    }

    private void moveTo(boolean blue, double speed) {
        double distance = this.zombie.getLocation()
                .distance(this.currentPathPoint.getLocation().middleBlock());

        if (distance > 1.5 && this.lastDirectionBlue == blue) {
            return;
        }

        this.lastDirectionBlue = blue;

        PathPoint next = this.currentPathPoint;

        while (next != null
                && next.getLocation().distance(this.currentPathPoint.getLocation().middleBlock())
                <= 2.1) {
            if (blue) {
                next = next.getNextToBlue();
            } else {
                next = next.getNextToRed();
            }
        }

        if (next == null) {
            if (distance < 1.2) {
                PushServer.onFinishReached(!blue);
                return;
            }

            return;
        }

        this.currentPathPoint = next;

        this.pathfinder.setSpeed(speed);
    }

}
