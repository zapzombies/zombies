package io.github.zap.zombies.game.equipment2.feature;

import io.github.zap.zombies.game.equipment2.Equipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface Feature {

    void onLeftClick(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                     @Nullable Consumer<ItemStack> onVisualUpdate);

    void onRightClick(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                      @Nullable Consumer<ItemStack> onVisualUpdate);

    void onSelected(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                    @Nullable Consumer<ItemStack> onVisualUpdate);

    void onDeselected(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                      @Nullable Consumer<ItemStack> onVisualUpdate);

    @Nullable ItemStack getVisual(@NotNull Equipment equipment);

}
