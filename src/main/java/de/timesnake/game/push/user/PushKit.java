package de.timesnake.game.push.user;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.loungebridge.util.user.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.List;

public class PushKit extends Kit {

    public static final PushKit BARBAR = new PushKit(1, "Barbar", Material.IRON_AXE,
            List.of("§7Axe and medium armor"),
            List.of(new ExItemStack(Material.CHAINMAIL_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.CHAINMAIL_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.CHAINMAIL_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.CHAINMAIL_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).setDropable(false),
                    new ExItemStack(0, Material.STONE_AXE).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.SPLASH_POTION, PotionType.INSTANT_HEAL, false, true).asQuantity(3).setSlot(1).setDropable(false),
                    new ExItemStack(Material.BEEF, 8).setSlot(8)));

    public static final PushKit ARCHER = new PushKit(2, "Archer", Material.BOW, List.of("§7Bow and low armor"),
            List.of(new ExItemStack(Material.GOLDEN_HELMET).setSlot(EquipmentSlot.HEAD).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.LEATHER_CHESTPLATE).setSlot(EquipmentSlot.CHEST).setUnbreakable(true).addExEnchantment(Enchantment.PROTECTION_PROJECTILE, 2).setDropable(false),
                    new ExItemStack(Material.GOLDEN_LEGGINGS).setSlot(EquipmentSlot.LEGS).setUnbreakable(true).setDropable(false),
                    new ExItemStack(Material.LEATHER_BOOTS).setSlot(EquipmentSlot.FEET).setUnbreakable(true).addExEnchantment(Enchantment.PROTECTION_FALL, 3).setDropable(false),
                    new ExItemStack(0, Material.WOODEN_SWORD).setUnbreakable(true).addExEnchantment(Enchantment.KNOCKBACK, 2).setDropable(false),
                    new ExItemStack(1, Material.BOW).setUnbreakable(true).setDropable(false),
                    new ExItemStack(6, Material.ARROW).asQuantity(32),
                    new ExItemStack(7, Material.SPECTRAL_ARROW).asQuantity(8),
                    new ExItemStack(8, Material.GOLDEN_CARROT).asQuantity(8)));

    public static final PushKit[] KITS = {BARBAR, ARCHER};

    public PushKit(Integer id, String name, Material material, List<String> description, List<ItemStack> items) {
        super(id, name, material, description, items);
    }
}
