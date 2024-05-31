/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserBlockPlaceEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.server.PushServer;
import net.minecraft.world.entity.monster.Zombie;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffectType;

public class UserManager implements Listener {

  public UserManager() {
    Server.registerListener(this, GamePush.getPlugin());
  }

  @EventHandler
  public void onBlockPlace(UserBlockPlaceEvent e) {
    if (!e.getBlockPlaced().getType().equals(Material.TNT)) {
      return;
    }

    Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);

    TNTPrimed tnt = loc.getWorld().spawn(loc, TNTPrimed.class);

    tnt.setFuseTicks(30);

    e.getUser().removeCertainItemStack(PushKitManager.TNT.asOne());
  }

  @EventHandler
  public void onExplosion(EntityExplodeEvent e) {
    e.setYield(0);
    e.blockList().clear();
  }

  @EventHandler
  public void onUserDropItem(UserDropItemEvent e) {
    Material material = e.getItemStack().getType();

    if (material.equals(Material.GLASS_BOTTLE)) {
      e.setCancelled(true);
      Server.runTaskLaterSynchrony(() -> e.getUser().getInventory().remove(material), 1, GamePush.getPlugin());
    }
  }

  @EventHandler
  public void onPotionSplash(PotionSplashEvent e) {
    Zombie zombie = PushServer.getEscordManager().getZombie();

    if (zombie != null) {
      Server.runTaskLaterSynchrony(() -> zombie.getBukkitCreature().removePotionEffect(PotionEffectType.SPEED),
          1, GamePush.getPlugin());
    }
  }

  @EventHandler
  public void onUserDamage(UserDamageEvent e) {
    User user = e.getUser();

    if (((PushUser) user).getKit() == null || !((PushUser) user).getKit().equals(PushKitManager.BARBAR)) {
      return;
    }

    if (user.getHealth() > 6) {
      return;
    }

    user.addPotionEffect(PotionEffectType.STRENGTH, 5 * 20, 1);
  }

}
