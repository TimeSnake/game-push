/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractListener;
import de.timesnake.game.push.main.GamePush;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpecialItemManager implements UserInventoryInteractListener {

  public static final SpecialItem JUMP_BOOST = new SpecialItem(new ExItemStack(Material.RABBIT_FOOT,
      "§6Jump Boost (5s)").setDropable(false).setMoveable(false), 10 * 20) {
    @Override
    public void onUse(User user) {
      user.addPotionEffect(PotionEffectType.JUMP_BOOST, 5 * 20, 2);
    }
  };

  public static final SpecialItem TURTLE_MASTER = new SpecialItem(
      new ExItemStack(Material.TURTLE_HELMET,
          "§6Turtle Mode (15s)").setDropable(false).setMoveable(false), 30 * 20) {
    @Override
    public void onUse(User user) {
      user.addPotionEffect(PotionEffectType.RESISTANCE, 15 * 20, 1);
      user.addPotionEffect(PotionEffectType.SLOWNESS, 15 * 20, 3);
      user.addPotionEffect(PotionEffectType.BLINDNESS, 13 * 20, 1);

      user.setItem(EquipmentSlot.HEAD, new ExItemStack(Material.NETHERITE_HELMET)
          .setUnbreakable(true).setMoveable(false).setDropable(false));
      user.setItem(EquipmentSlot.CHEST, new ExItemStack(Material.NETHERITE_CHESTPLATE)
          .setUnbreakable(true).setMoveable(false).setDropable(false));
      user.setItem(EquipmentSlot.LEGS, new ExItemStack(Material.NETHERITE_LEGGINGS)
          .setUnbreakable(true).setMoveable(false).setDropable(false));
      user.setItem(EquipmentSlot.FEET, new ExItemStack(Material.NETHERITE_BOOTS)
          .setUnbreakable(true).setMoveable(false).setDropable(false));

      Server.runTaskLaterSynchrony(() -> {
        user.setItem(EquipmentSlot.HEAD, new ExItemStack(Material.DIAMOND_HELMET)
            .setUnbreakable(true).setMoveable(false).setDropable(false));
        user.setItem(EquipmentSlot.CHEST, new ExItemStack(Material.DIAMOND_CHESTPLATE)
            .setUnbreakable(true).setMoveable(false).setDropable(false));
        user.setItem(EquipmentSlot.LEGS, new ExItemStack(Material.DIAMOND_LEGGINGS)
            .setUnbreakable(true).setMoveable(false).setDropable(false));
        user.setItem(EquipmentSlot.FEET, new ExItemStack(Material.DIAMOND_BOOTS)
            .setUnbreakable(true).setMoveable(false).setDropable(false));
      }, 15 * 20, GamePush.getPlugin());
    }
  };

  public static final List<SpecialItem> ITEMS = List.of(JUMP_BOOST, TURTLE_MASTER);


  private final HashMap<ExItemStack, SpecialItem> specialItems = new HashMap<>();

  public SpecialItemManager() {
    for (SpecialItem specialItem : ITEMS) {
      this.specialItems.put(specialItem.getItem(), specialItem);
    }

    Server.getInventoryEventManager().addInteractListener(this,
        this.specialItems.keySet().toArray(new ExItemStack[0]));
  }

  @Override
  public void onUserInventoryInteract(UserInventoryInteractEvent event) {
    SpecialItem item = this.specialItems.get(event.getClickedItem());

    User user = event.getUser();

    if (item == null || item.containsCooldownUser(user)) {
      return;
    }

    item.addCooldownUser(user);

    item.onUse(event.getUser());
  }

  public static abstract class SpecialItem {

    private final ExItemStack item;

    private final int cooldown;
    private final Set<User> cooldownUsers = new HashSet<>();

    public SpecialItem(ExItemStack item, int cooldown) {
      this.item = item;
      this.cooldown = cooldown;
    }

    public ExItemStack getItem() {
      return item;
    }

    public int getCooldown() {
      return cooldown;
    }

    public void addCooldownUser(User user) {
      this.cooldownUsers.add(user);

      int slot = user.getInventory().getHeldItemSlot();

      user.getInventory().setItem(slot, this.item.cloneWithId().disenchant());

      Server.runTaskLaterSynchrony(() -> {
        this.cooldownUsers.remove(user);
        user.getInventory().setItem(slot, this.item.cloneWithId().enchant());
      }, this.cooldown, GamePush.getPlugin());
    }

    public boolean containsCooldownUser(User user) {
      return this.cooldownUsers.contains(user);
    }

    public abstract void onUse(User user);
  }
}
