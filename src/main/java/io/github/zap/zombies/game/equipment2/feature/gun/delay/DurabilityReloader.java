package io.github.zap.zombies.game.equipment2.feature.gun.delay;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.equipment2.Equipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class DurabilityReloader implements VisualDelayer {

    private final Zombies zombies;

    private final MiniMessage miniMessage;

    private final Sound reloadSound;

    private final long reloadTime;

    public DurabilityReloader(@NotNull Zombies zombies, @NotNull MiniMessage miniMessage, @NotNull Sound reloadSound,
                              long reloadTime) {
        this.zombies = zombies;
        this.miniMessage = miniMessage;
        this.reloadSound = reloadSound;
        this.reloadTime = reloadTime;
    }

    @Override
    public @NotNull BukkitTask delay(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                                     @Nullable Consumer<ItemStack> onVisualUpdate, @NotNull Runnable onReloadEnd) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            bukkitPlayer.playSound(reloadSound);
        }

        ItemStack stack = equipment.getStack();
        if (stack == null) {
            throw new IllegalArgumentException("Tried to reload an equipment with a null stack!");
        }

        Component reloading = miniMessage.parse("<red><bold>RELOADING");
        return new BukkitRunnable() {

            private final int maxDurability = stack.getType().getMaxDurability();

            private int step = 0;

            @Override
            public void run() {
                if (step < reloadTime) {
                    setItemDamage((int) Math.round(maxDurability - (double) (++step * maxDurability) / reloadTime));
                    if (equipment.isSelected()) {
                        Player bukkitPlayer = player.getPlayer();
                        if (bukkitPlayer != null) {
                            bukkitPlayer.sendActionBar(reloading);
                        }
                    }
                }
                else {
                    if (equipment.isSelected()) {
                        Player bukkitPlayer = player.getPlayer();
                        if (bukkitPlayer != null) {
                            bukkitPlayer.sendActionBar(Component.empty());
                        }
                    }

                    cancel();
                    onReloadEnd.run();
                }
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                super.cancel();
                setItemDamage(0);
            }

            private void setItemDamage(int damage) {
                ItemStack stack = equipment.getStack();
                if (stack != null) {
                    ItemMeta meta = stack.getItemMeta();
                    if (meta instanceof Damageable damageable) {
                        damageable.setDamage(damage);
                        stack.setItemMeta(meta);
                        if (onVisualUpdate != null) {
                            onVisualUpdate.accept(stack);
                        }
                    }
                }
            }
        }.runTaskTimer(zombies, 0L, 1L);
    }

}
