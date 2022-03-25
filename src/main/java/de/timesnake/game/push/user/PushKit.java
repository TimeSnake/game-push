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
            List.of("§7Sword and strong armor"),
            List.of(new ExItemStack(Material.CHAINMAIL_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.IRON_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.CHAINMAIL_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.DIAMOND_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).setDropable(false),
                    new ExItemStack(0, Material.STONE_SWORD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.POTION, PotionType.INSTANT_HEAL, false, true).asQuantity(3).setSlot(1).setDropable(false),
                    FOOD.cloneWithId().asQuantity(8).setSlot(8)));

    public static final PushKit ARCHER = new PushKit(2, "Archer", Material.BOW, List.of("§7Bow and medium armor"),
            List.of(new ExItemStack(Material.DIAMOND_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.CHAINMAIL_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 2).setDropable(false),
                    new ExItemStack(Material.GOLDEN_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.LEATHER_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).addExEnchantment(Enchantment.PROTECTION_FALL, 3).setDropable(false),
                    new ExItemStack(0, Material.WOODEN_SWORD).setUnbreakable(true).addExEnchantment(Enchantment.KNOCKBACK, 2).setDropable(false),
                    new ExItemStack(1, Material.BOW).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.ARROW_DAMAGE, 1),
                    new ExItemStack(true, "§6Poison", PotionEffectType.POISON, 15 * 20, 2, 1).setSlot(2),
                    new ExItemStack(6, Material.ARROW).asQuantity(64),
                    new ExItemStack(7, Material.SPECTRAL_ARROW).asQuantity(8),
                    new ExItemStack(8, Material.GOLDEN_CARROT).asQuantity(8)));

    public static final PushKit ASSASSIN = new PushKit(3, "Assassin", Material.FEATHER, List.of("§7Speed and low " +
            "armor"),
            List.of(new ExItemStack(Material.TURTLE_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.GOLDEN_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.LEATHER_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.GOLDEN_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).setDropable(false).addExEnchantment(Enchantment.PROTECTION_FALL, 4),
                    new ExItemStack(0, Material.GOLDEN_SWORD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(false, "§6Speed (30s)", PotionEffectType.SPEED, 30 * 20, 2, 1).setSlot(1).setDropable(false),
                    new ExItemStack(true, "§6Speed (15s)", PotionEffectType.SPEED, 15 * 20, 2, 2).setSlot(2).setDropable(false),
                    FOOD.cloneWithId().asQuantity(8).setSlot(8)));


    public static final PushKit[] KITS = {BARBAR, ARCHER, ASSASSIN};

    public PushKit(Integer id, String name, Material material, List<String> description, List<ItemStack> items) {
        super(id, name, material, description, items);
    }
}
