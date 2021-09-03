package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.UpgradeableEquipmentObjectGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Object group of perks
 */
public class PerkObjectGroup extends UpgradeableEquipmentObjectGroup {

    public PerkObjectGroup(@NotNull Player player, @NotNull Set<Integer> slots) {
        super(player, slots, false, false);
    }

    @Override
    public @NotNull ItemStack createPlaceholderItemStack(int placeholderNumber) {
        ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
        TextComponent name = Component.text(String.format("Perk #%d", placeholderNumber)).color(NamedTextColor.BLUE);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(name);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public boolean isObjectRecommendedEquipment(@NotNull HotbarObject hotbarObject) {
        return hotbarObject instanceof Perk;
    }

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentObjectGroupType.PERK.name();
    }
}
