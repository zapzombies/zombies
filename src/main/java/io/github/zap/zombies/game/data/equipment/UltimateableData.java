package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.util.RomanNumeral;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for a piece of equipment which can be ultimated using the ultimate machine
 * @param <L> The type of the equipment levels
 */
public abstract class UltimateableData<L> extends EquipmentData<L> {

    public UltimateableData(@NotNull String type, @NotNull String name, @NotNull String displayName,
                            @NotNull Material material, @NotNull List<String> lore, @NotNull List<L> levels) {
        super(type, name, displayName, material, lore, levels);
    }

    protected UltimateableData() {

    }


    @NotNull
    @Override
    public ItemStack createItemStack(@NotNull Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (level > 0) {
            itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @NotNull
    @Override
    public String getFormattedDisplayName(@NotNull Player player, int level) {
        String formattedDisplayName = getDisplayName();
        if (level > 0) {
            formattedDisplayName = ChatColor.BOLD.toString() + formattedDisplayName;
            formattedDisplayName += " Ultimate";

            if (level > 1) {
                formattedDisplayName += " " + RomanNumeral.toRoman(level);
            }
        }

        return formattedDisplayName;
    }

}
