/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.push.user;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.loungebridge.util.user.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class PushKit extends Kit {

    public static final ExItemStack FOOD = new ExItemStack(Material.COOKED_BEEF);
    public static final ExItemStack TNT = new ExItemStack(Material.TNT);

    public static final PushKit BARBAR = new PushKit(1, "Barbar", Material.IRON_AXE,
            List.of("§7Stone Sword", "§7Iron, Chainmail Armor", "", "§7Instant Heal, Strength"),
            List.of(new ExItemStack(Material.DIAMOND_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.IRON_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.IRON_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.DIAMOND_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).setDropable(false),
                    new ExItemStack(0, Material.STONE_SWORD).setUnbreakable(true).setDropable(false),
                    ExItemStack.getPotion(Material.POTION, PotionType.INSTANT_HEAL, false, true).asQuantity(3).setSlot(1).setDropable(false),
                    FOOD.cloneWithId().asQuantity(8).setSlot(8)));

    public static final PushKit ARCHER = new PushKit(2, "Archer", Material.BOW,
            List.of("§7Bow, Wooden Sword", "§7Chainmail, Golden Armor", "", "§7Poison Potion"),
            List.of(new ExItemStack(Material.DIAMOND_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.GOLDEN_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 2).setDropable(false),
                    new ExItemStack(Material.GOLDEN_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.LEATHER_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).addExEnchantment(Enchantment.PROTECTION_FALL, 3).setDropable(false),
                    new ExItemStack(0, Material.WOODEN_SWORD).setUnbreakable(true).addExEnchantment(Enchantment.KNOCKBACK, 1).setDropable(false),
                    new ExItemStack(1, Material.BOW).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.ARROW_DAMAGE, 1),
                    ExItemStack.getPotion(ExItemStack.PotionMaterial.SPLASH, 1, "§6Poison", PotionEffectType.POISON, 10 * 20, 2).setSlot(2),
                    new ExItemStack(6, Material.ARROW).asQuantity(64),
                    new ExItemStack(7, Material.SPECTRAL_ARROW).asQuantity(8),
                    new ExItemStack(8, Material.GOLDEN_CARROT).asQuantity(8)));

    public static final PushKit ASSASSIN = new PushKit(3, "Assassin", Material.FEATHER,
            List.of("§7Golden Sword", "§7Golden Armor", "", "§7Speed Potion"),
            List.of(new ExItemStack(Material.TURTLE_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 1),
                    new ExItemStack(Material.GOLDEN_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 1),
                    new ExItemStack(Material.LEATHER_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 1),
                    new ExItemStack(Material.GOLDEN_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.DEPTH_STRIDER, 4),
                    new ExItemStack(0, Material.GOLDEN_SWORD).addExEnchantment(Enchantment.DAMAGE_ALL, 5).setUnbreakable(true).setDropable(false),
                    ExItemStack.getPotion(ExItemStack.PotionMaterial.DRINK, 1, "§6Speed (30s)", PotionEffectType.SPEED, 30 * 20, 1).setSlot(1).setDropable(false),
                    ExItemStack.getPotion(ExItemStack.PotionMaterial.SPLASH, 2, "§6Speed (15s)", PotionEffectType.SPEED, 15 * 20, 1).setSlot(2).setDropable(false),
                    FOOD.cloneWithId().asQuantity(8).setSlot(8)));

    /*
    public static final PushKit THIEF = new PushKit(4, "Thief", Material.LEATHER,
            List.of("§7Iron Sword", "§7Leather Armor", "", "§7Jump Boost, Invisibility"),
            List.of(new ExItemStack(Material.LEATHER_HELMET, Color.BLACK).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 1),
                    new ExItemStack(Material.LEATHER_CHESTPLATE, Color.BLACK).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 1),
                    new ExItemStack(Material.LEATHER_LEGGINGS, Color.BLACK).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 1),
                    new ExItemStack(Material.LEATHER_BOOTS, Color.BLACK).setSlot(EquipmentSlot.FEET).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_FALL, 4),
                    new ExItemStack(0, Material.IRON_SWORD).setUnbreakable(true).setDropable(false),
                    SpecialItemManager.JUMP_BOOST.getItem().cloneWithId().setSlot(1).enchant(),
                    new ExItemStack(false, "§6Invisibility (7s)", PotionEffectType.INVISIBILITY, 7 * 20, 1, 1).setSlot(2).setDropable(false),
                    FOOD.cloneWithId().asQuantity(8).setSlot(8)));
     */

    public static final PushKit KNIGHT = new PushKit(5, "Knight", Material.DIAMOND_CHESTPLATE,
            List.of("§7Netherite Shovel", "§7Diamond Armor", "", "§7Turtle Master"),
            List.of(new ExItemStack(Material.DIAMOND_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.DIAMOND_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.DIAMOND_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.DIAMOND_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).setDropable(false),
                    new ExItemStack(0, Material.DIAMOND_SHOVEL).addExEnchantment(Enchantment.SWEEPING_EDGE, 2).setUnbreakable(true).setDropable(false),
                    SpecialItemManager.TURTLE_MASTER.getItem().cloneWithId().setSlot(1).enchant(),
                    //new ExItemStack(false, "§6Regeneration (10s)", PotionEffectType.REGENERATION, 10 * 20, 2, 1).setSlot(7).setDropable(false),
                    FOOD.cloneWithId().asQuantity(16).setSlot(8)));


    public static final PushKit[] KITS = {BARBAR, ARCHER, ASSASSIN, KNIGHT}; // THIEF

    public PushKit(Integer id, String name, Material material, List<String> description, List<ItemStack> items) {
        super(id, name, material, description, items);
    }
}
