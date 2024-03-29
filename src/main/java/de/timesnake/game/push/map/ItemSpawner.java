/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.user.PushKitManager;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

public class ItemSpawner {

  private static final List<ExItemStack> ITEMS = List.of(new ExItemStack(Material.GOLDEN_APPLE),
      PushKitManager.TNT,
      PushKitManager.FOOD.cloneWithId().asQuantity(3), new ExItemStack(Material.ENDER_PEARL));

  private static final int DELAY_BASE = 10; // sec * 2
  private static final int DELAY_RANGE = 10; // sec * 2

  private final ExLocation location;
  private BukkitTask task;

  private Random random = new Random();
  private int delay = 15;

  public ItemSpawner(ExLocation location) {
    this.location = location;
  }

  public void start() {
    this.task = Server.runTaskTimerSynchrony(() -> {
      delay--;

      if (delay <= 0) {
        this.location.getWorld()
            .dropItem(this.location, ITEMS.get(this.random.nextInt(ITEMS.size())));
        this.delay = this.random.nextInt(DELAY_RANGE) + DELAY_BASE;
      }
    }, 20, 40, GamePush.getPlugin());
  }

  public void stop() {
    if (this.task != null) {
      this.task.cancel();
    }
  }

}
