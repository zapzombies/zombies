package io.github.zap.zombies.game.equipment.gun;

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
 * Object group of guns
 */
public class GunObjectGroup extends UpgradeableEquipmentObjectGroup {

    public GunObjectGroup(@NotNull Player player, @NotNull Set<Integer> slots) {
        super(player, slots, true, true);
    }

    @Override
    public @NotNull ItemStack createPlaceholderItemStack(int placeholderNumber) {
        ItemStack itemStack = new ItemStack(Material.LIGHT_GRAY_DYE);
        TextComponent name = Component.text(String.format("Gun #%d", placeholderNumber)).color(NamedTextColor.GOLD);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(name);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public boolean isObjectRecommendedEquipment(@NotNull HotbarObject hotbarObject) {
        return hotbarObject instanceof Gun<?, ?>;
    }

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentObjectGroupType.GUN.name();
    }

    @Override
    public boolean isUltimateable() {
        return true;
    }

}
