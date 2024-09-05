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
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.entities.EntityManager;
import de.timesnake.library.entities.entity.ZombieBuilder;
import de.timesnake.library.entities.pathfinder.UpdatedLocationGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class EscortManager {

  private static final double RADIUS = 5;
  private static final double DEFAULT_SPEED = 1.1;
  private static final double MAX_SPEED = 1.7;
  private static final double SPEED_INCREASE = 30;
  private static final int STOP_PLAYER_NUMBER = 2;

  private final Logger logger = LogManager.getLogger("push.escort");

  private Zombie zombie;
  private PathPoint currentPathPoint;
  private boolean lastDirectionBlue;

  private BukkitTask moveTask;

  private UpdatedLocationGoal pathfinder;

  private int time;
  private double baseSpeed;

  public EscortManager() {

  }

  public Zombie getZombie() {
    return zombie;
  }

  public void start() {
    for (org.bukkit.entity.Zombie zombie : PushServer.getMap().getWorld().getEntitiesByClass(org.bukkit.entity.Zombie.class)) {
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
      this.zombie.remove(Entity.RemovalReason.DISCARDED);
    }
  }

  public void spawn() {
    PushMap map = PushServer.getMap();

    this.currentPathPoint = map.getSpawnPoint();

    if (this.zombie != null) {
      this.zombie.remove(Entity.RemovalReason.DISCARDED);
    }

    ExLocation spawn = map.getZombieSpawn();

    this.zombie = new ZombieBuilder()
        .clearGoalTargets()
        .clearPathfinderGoals()
        .setMaxHealth(2048)
        .applyOnEntity(e -> {
          e.setInvulnerable(true);
          e.setPersistenceRequired(true);
          e.setCustomName(Component.literal("Zombie"));
          e.setCustomNameVisible(false);
          e.setItemSlot(EquipmentSlot.HEAD, new ExItemStack(Material.GOLDEN_HELMET).setUnbreakable(true).getHandle());
          e.collides = false;
          e.setHealth(2048);
          e.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
        })
        .addPathfinderGoal(1, e -> {
          this.pathfinder = new UpdatedLocationGoal(e, this.baseSpeed, 32, 0.2) {
            @Override
            public Location getNextLocation(Location entityLoc) {
              return EscortManager.this.currentPathPoint.getLocation();
            }
          };
          return this.pathfinder;
        })
        .build(map.getWorld().getHandle());

    EntityManager.spawnEntity(map.getWorld().getBukkitWorld(), this.zombie);

    this.logger.info("Spawned zombie");
  }

  public void runMoveTask() {
    this.moveTask = Server.runTaskTimerSynchrony(() -> {
      int countBlue = 0;
      int countRed = 0;

      for (Player player : this.zombie.getBukkitEntity().getLocation().getNearbyPlayers(RADIUS)) {
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
    double distance = this.zombie.getBukkitLivingEntity().getLocation()
        .distance(this.currentPathPoint.getLocation().middleHorizontalBlock());

    if (distance > 1.5 && this.lastDirectionBlue == blue) {
      return;
    }

    this.lastDirectionBlue = blue;

    PathPoint next = this.currentPathPoint;

    while (next != null && next.getLocation().distance(this.currentPathPoint.getLocation().middleHorizontalBlock()) <= 2.1) {
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
