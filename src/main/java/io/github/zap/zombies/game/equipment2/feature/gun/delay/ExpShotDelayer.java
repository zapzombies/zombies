package io.github.zap.zombies.game.equipment2.feature.gun.delay;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.State;
import io.github.zap.zombies.game.equipment2.Equipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class ExpShotDelayer implements VisualDelayer {

    private final Zombies zombies;

    private final State<Double> delayModifier;

    private final long delayTime;

    public ExpShotDelayer(@NotNull Zombies zombies, @NotNull State<Double> delayModifier, long delayTime) {
        this.zombies = zombies;
        this.delayModifier = delayModifier;
        this.delayTime = delayTime;
    }

    @Override
    public @NotNull BukkitTask delay(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                                     @Nullable Consumer<ItemStack> onVisualUpdate, @NotNull Runnable onDelayEnd) {
        Double delayFactor = delayModifier.getValue();
        if (delayFactor == null) {
            throw new IllegalStateException("Tried to delay when delay modifier state was null!");
        }

        return new BukkitRunnable() {

            private final int ticks = (int) Math.round((delayTime * delayFactor));

            private final float perStep = 1F / ticks;

            private int step = 0;

            @Override
            public void run() {
                if (step < ticks) {
                    step++;
                    if (equipment.isSelected()) {
                        setExp(step * perStep);
                    }
                }
                else {
                    cancel();
                    onDelayEnd.run();
                }
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                super.cancel();
                setExp(1);
            }

            private void setExp(float exp) {
                Player bukkitPlayer = player.getPlayer();
                if (bukkitPlayer != null) {
                    bukkitPlayer.setExp(exp);
                }
            }

        }.runTaskTimer(zombies, 0L, 1L);
    }

}
