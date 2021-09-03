package io.github.zap.zombies.game.equipment2.feature.gun;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment2.Equipment;
import io.github.zap.zombies.game.equipment2.feature.Feature;
import io.github.zap.zombies.game.equipment2.feature.gun.delay.VisualDelayer;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Gun implements Feature {

    private final Zombies zombies;

    private final MiniMessage miniMessage;

    private final BukkitScheduler scheduler;

    private final MapData map;

    private final Set<Mob> mobSet;

    private final Material material;

    private final Component displayName;

    private final List<Component> lore;

    private final Map<Enchantment, Integer> enchantments;

    private final Collection<Shot> shots;

    private final Sound sound;

    private final VisualDelayer shotDelayer;

    private final VisualDelayer reloader;

    private final int maxAmmo;

    private final int maxClip;

    private final long delayBetweenShots;

    private int ammo;

    private int clip;

    private boolean canShoot;

    private boolean canReload;

    public Gun(@NotNull Zombies zombies, @NotNull MiniMessage miniMessage, @NotNull MapData map,
               @NotNull Set<Mob> mobSet, @NotNull Material material, @NotNull Component displayName,
               @NotNull List<Component> lore, @NotNull Map<Enchantment, Integer> enchantments,
               @NotNull Collection<Shot> shots, @NotNull Sound sound, @NotNull VisualDelayer shotDelayer,
               @NotNull VisualDelayer reloader, int maxAmmo, int maxClip, long delayBetweenShots) {
        this.zombies = zombies;
        this.miniMessage = miniMessage;
        this.scheduler = zombies.getServer().getScheduler();
        this.map = map;
        this.mobSet = mobSet;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.enchantments = enchantments;
        this.shots = shots;
        this.sound = sound;
        this.shotDelayer = shotDelayer;
        this.reloader = reloader;
        this.maxAmmo = maxAmmo;
        this.maxClip = maxClip;
        this.delayBetweenShots = delayBetweenShots;
        this.ammo = maxAmmo;
        this.clip = maxClip;
    }

    @Override
    public void onLeftClick(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                            @Nullable Consumer<ItemStack> onVisualUpdate) {
        doReload(equipment, player, onVisualUpdate);
    }

    @Override
    public void onRightClick(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                             @Nullable Consumer<ItemStack> onVisualUpdate) {
        if (canShoot) {
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer != null) {
                canShoot = false;
                canReload = false;
                Set<Mob> used = new HashSet<>();

                if (!shots.isEmpty()) {
                    Iterator<Shot> iterator = shots.iterator();
                    doShot(equipment, iterator.next(), player, used, onVisualUpdate);

                    int remainingShots = clip;
                    long cumulativeDelay = 0L;
                    while (iterator.hasNext() && remainingShots > 0) {
                        remainingShots--;

                        if (player.isInGame()) {
                            Player nextBukkitPlayer = player.getPlayer();
                            if (nextBukkitPlayer != null) {
                                boolean lastShot = iterator.hasNext();
                                cumulativeDelay += delayBetweenShots;
                                scheduler.runTaskLater(zombies, () -> {
                                    doShot(equipment, iterator.next(), player, used, onVisualUpdate);
                                    if (lastShot) {
                                        doShotDelay(equipment, player, onVisualUpdate);
                                    }
                                }, cumulativeDelay);
                            }
                        } else break;
                    }
                }
            } else throw new IllegalArgumentException("Tried to shoot for a player that is not online!");
        }
    }

    @Override
    public void onSelected(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                           @Nullable Consumer<ItemStack> onVisualUpdate) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            bukkitPlayer.setLevel(ammo);
        }
    }

    @Override
    public void onDeselected(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                             @Nullable Consumer<ItemStack> onVisualUpdate) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            bukkitPlayer.setLevel(0);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void doShot(@NotNull Equipment equipment, @NotNull Shot shot, @NotNull ZombiesPlayer player,
                        @NotNull Set<Mob> used, @Nullable Consumer<ItemStack> onVisualUpdate) {
        Location eyeLocation = player.getPlayer().getEyeLocation();
        shot.shoot(map, player.getPlayer().getWorld(), player, mobSet, used, eyeLocation.toVector(),
                eyeLocation.getDirection(), new ArrayList<>());

        player.getPlayer().getWorld().playSound(sound, eyeLocation.getX(), eyeLocation.getY(), eyeLocation.getZ());
        clip--;
        ammo--;
        updateAfterShooting(equipment, player.getPlayer(), onVisualUpdate);
    }

    private void updateAfterShooting(@NotNull Equipment equipment, @NotNull Player player,
                                     @Nullable Consumer<ItemStack> onVisualUpdate) {
        if (equipment.isSelected()) {
            player.setLevel(ammo);
        }
        ItemStack stack = equipment.getStack();
        if (stack != null) {
            stack.setAmount(clip);
            if (onVisualUpdate != null) {
                onVisualUpdate.accept(stack);
            }
        }
    }

    private void doShotDelay(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                             @Nullable Consumer<ItemStack> onVisualUpdate) {
        shotDelayer.delay(equipment, player, onVisualUpdate, () -> {
            if (clip == 0) {
                if (ammo == 0) {
                    Player bukkitPlayer = player.getPlayer();
                    if (bukkitPlayer != null) {
                        bukkitPlayer.sendMessage(miniMessage.parse("<red>You don't have any ammo!"));
                    }
                } else doReload(equipment, player, onVisualUpdate);
            } else {
                canReload = true;
                canShoot = true;
            }
        });
    }

    private void doReload(@NotNull Equipment equipment, @NotNull ZombiesPlayer player,
                          @Nullable Consumer<ItemStack> onVisualUpdate) {
        if (canReload) {
            canReload = false;
            reloader.delay(equipment, player, onVisualUpdate, () -> {
                clip = Math.min(maxClip, ammo);

                canReload = true;
                canShoot = true;
            });
        }
    }

    @Override
    public @Nullable ItemStack getVisual(@NotNull Equipment equipment) {
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
