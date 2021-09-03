package io.github.zap.zombies.game.equipment2.feature.gun.delay;

import io.github.zap.zombies.game.equipment2.Equipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface VisualDelayer {

    @NotNull BukkitTask delay(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                              @Nullable Consumer<ItemStack> onVisualUpdate, @NotNull Runnable onDelayEnd);

}
