/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.user;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.game.push.server.PushServer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class PushUser extends GameUser {

  private static final List<ExItemStack> DEFAULT =
      List.of(new ExItemStack(0, Material.STONE_SWORD).setUnbreakable(true),
          new ExItemStack(1, Material.BOW).setUnbreakable(true),
          new ExItemStack(7, Material.ARROW).asQuantity(8),
          new ExItemStack(8, Material.BEEF).asQuantity(8),
          new ExItemStack(Material.CHAINMAIL_HELMET).setUnbreakable(true)
              .setSlot(EquipmentSlot.HEAD),
          new ExItemStack(Material.GOLDEN_CHESTPLATE).setUnbreakable(true)
              .setSlot(EquipmentSlot.CHEST),
          new ExItemStack(Material.CHAINMAIL_LEGGINGS).setUnbreakable(true)
              .setSlot(EquipmentSlot.LEGS),
          new ExItemStack(Material.GOLDEN_BOOTS).setUnbreakable(true)
              .setSlot(EquipmentSlot.FEET));


  public PushUser(Player player) {
    super(player);
  }

  @Override
  public void onGameJoin() {
    super.onGameJoin();

    if (this.getTeam().equals(PushServer.getGame().getBlueTeam())) {
      this.teleport(PushServer.getMap().getRandomBlueSpawn());
    } else {
      this.teleport(PushServer.getMap().getRandomRedSpawn());
    }

    this.setItems();
    this.setGameMode(GameMode.SURVIVAL);

    this.setInvulnerable(true);
    this.lockLocation();

    this.setBossBar(PushServer.getBossBar());
  }

  private void setItems() {
    if (PushServer.areKitsEnabled()) {
      this.applyKit();
    } else {
      int i = 0;
      for (ExItemStack item : DEFAULT) {
        if (item.getSlot() != null) {
          this.getInventory().setItem(item.getSlot(), item.cloneWithId());
        } else {
          this.getInventory().setItem(i, item.cloneWithId());
          i++;
        }
      }
    }
  }

  @Override
  public ExLocation getRespawnLocation() {
    this.clearInventory();
    this.removePotionEffects();
    this.heal();
    this.setFireTicks(0);

    this.kitLoaded = false;
    this.setItems();

    return null;
  }
}
