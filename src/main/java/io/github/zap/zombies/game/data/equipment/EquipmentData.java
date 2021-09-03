package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.util.RomanNumeral;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a piece of generic equipment
 * @param <L> The type of the equipment levels
 */
@AllArgsConstructor
@Getter
public abstract class EquipmentData<L> {

    private String type;

    private String name;

    private String displayName;

    private Material material;

    private List<String> lore;

    private List<L> levels;

    protected EquipmentData() {

    }

    /**
     * Creates an item stack that represents the equipment
     * @param player The player to create the item stack for and to get the locale from
     * @param level The level of the equipment
     * @return An item stack representing the equipment
     */
    public @NotNull ItemStack createItemStack(@NotNull Player player, int level) {
        if (0 <= level && level < levels.size()) {
            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.displayName(Component.text(getFormattedDisplayName(player, level), getDefaultChatColor()));
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            itemMeta.setLore(getLore(player, level));
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        } else {
            throw new IndexOutOfBoundsException(String.format("Level %d is not within the level bounds of [0, %d)!",
                    level, levels.size()));
        }
    }

    public @NotNull List<String> getLore(@NotNull Player player, int level) {
        List<String> lore = new ArrayList<>(getLore());
        lore.set(0, ChatColor.RESET.toString() + ChatColor.GRAY.toString() + lore.get(0));

        return lore;
    }

    /**
     * Gets the formatted version of the display name
     * @param player The player for which the equipment is given to
     * @param level The level of the equipment display
     * @return The formatted version of the display name
     */
    public @NotNull String getFormattedDisplayName(@NotNull Player player, int level) {
        String formattedDisplayName = displayName;
        if (level > 0) {
            formattedDisplayName = String.format(
                    "%s%s %s",
                    ChatColor.BOLD.toString(),
                    formattedDisplayName,
                    RomanNumeral.toRoman(level + 1)
            );
        }

        return formattedDisplayName;
    }

    /**
     * Gets the default chat color of the equipment
     * @return The default chat color of the equipment
     */
    public abstract @NotNull TextColor getDefaultChatColor();
    /**
     * Gets the equipment object group type of the equipment as a string
     * @return The equipment object group type of the equipment
     */
    public abstract @NotNull String getEquipmentObjectGroupType();

}
