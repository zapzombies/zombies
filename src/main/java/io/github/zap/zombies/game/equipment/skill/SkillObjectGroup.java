package io.github.zap.zombies.game.equipment.skill;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
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
 * Object group of skills
 */
public class SkillObjectGroup extends EquipmentObjectGroup {

    public SkillObjectGroup(@NotNull Player player, @NotNull Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public @NotNull ItemStack createPlaceholderItemStack(int placeholderNumber) {
        ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
        TextComponent name = Component.text(String.format("Skill #%d", placeholderNumber)).color(NamedTextColor.AQUA);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(name);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public boolean isObjectRecommendedEquipment(@NotNull HotbarObject hotbarObject) {
        return hotbarObject instanceof SkillEquipment;
    }

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentObjectGroupType.SKILL.name();
    }
}
