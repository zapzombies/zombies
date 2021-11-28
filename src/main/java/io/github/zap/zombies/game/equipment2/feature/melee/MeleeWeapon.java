package io.github.zap.zombies.game.equipment2.feature.melee;

import io.github.zap.zombies.game.equipment2.Equipment;
import io.github.zap.zombies.game.equipment2.feature.Feature;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class MeleeWeapon implements Feature {

    private final Material material;

    private final Component displayName;

    private final List<Component> lore;

    private final Map<Enchantment, Integer> enchantments;

    public MeleeWeapon(@NotNull Material material, @NotNull Component displayName, @NotNull List<Component> lore,
                       @NotNull Map<Enchantment, Integer> enchantments) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.enchantments = enchantments;
    }

    @Override
    public void onLeftClick(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                            @Nullable Consumer<ItemStack> onVisualUpdate) {

    }

    @Override
    public void onRightClick(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                             @Nullable Consumer<ItemStack> onVisualUpdate) {

    }

    @Override
    public void onSelected(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                           @Nullable Consumer<ItemStack> onVisualUpdate) {

    }

    @Override
    public void onDeselected(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                             @Nullable Consumer<ItemStack> onVisualUpdate) {

    }

    @Nullable
    @Override
    public ItemStack getVisual(@NotNull Equipment equipment) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(displayName);
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
            meta.addEnchant(enchantment.getKey(), enchantment.getValue(), true);
        }
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
