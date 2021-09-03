package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.arenaapi.DisposableBukkitRunnable;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.GunData;
import io.github.zap.zombies.game.data.equipment.gun.GunLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a basic gun
 * @param <D> The gun data type
 * @param <L> The gun level type
 */
@Getter
public abstract class Gun<D extends GunData<L>, L extends GunLevel> extends UpgradeableEquipment<D, L> {

    private int currentClipAmmo;

    private int currentAmmo;

    private boolean canReload = true;

    private int reloadTask = -1;

    private boolean canShoot = true;

    private int fireDelayTask = -1;

    private int shootingTask = -1;

    public Gun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, D equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);

        refill();
    }

    /**
     * Refills the gun completely
     */
    public void refill() {
        GunLevel gunLevel = getCurrentLevel();
        setAmmo(gunLevel.getAmmo());
        setClipAmmo(gunLevel.getClipAmmo());

        resetStates();
    }

    /**
     * Resets shooting or reloading states if applicable
     */
    public void resetStates() {
        BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
        if (reloadTask != -1) {
            bukkitScheduler.cancelTask(reloadTask);
        }
        if (fireDelayTask == -1) {
            canShoot = true;
        }
        if (shootingTask == -1) {
            canReload = true;
        }
    }

    /**
     * Reloads the gun
     */
    public void reload() {
        if (canReload) {
            GunLevel level = getCurrentLevel();
            int clipAmmo = level.getClipAmmo();

            if (currentClipAmmo < clipAmmo && currentAmmo > 0) {
                canReload = false;
                canShoot = false;

                Player player = getPlayer();
                player.playSound(Sound.sound(Key.key("minecraft:entity.horse.gallop"), Sound.Source.MASTER, 1F,
                        0.5F));

                reloadTask = getArena().runTaskTimer(0L, 1L, new DisposableBukkitRunnable() {

                    private final Component reloadingComponent = Component
                            .text("RELOADING")
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD);

                    private final int reloadRate = level.getReloadRate();
                    private final int maxVal = getEquipmentData().getMaterial().getMaxDurability();

                    private int step = 0;

                    @Override
                    public void run() {
                        if (step < reloadRate) {
                            setItemDamage(maxVal - (++step * maxVal) / reloadRate);
                            if (isSelected()) {
                                player.sendActionBar(reloadingComponent);
                            }
                        } else {
                            setItemDamage(0);
                            setClipAmmo(Math.min(clipAmmo, currentAmmo));

                            if (isSelected()) {
                                getPlayer().sendActionBar(Component.empty());
                            }

                            canReload = true;
                            if (shootingTask == -1 && fireDelayTask == -1 && currentAmmo > 0) {
                                canShoot = true;
                            }
                            reloadTask = -1;
                            cancel();
                        }
                    }

                }).getTaskId();
            }
        }
    }

    /**
     * Updates the item stack after shooting the gun
     * (you win the award for the longest method name in the plugin, congratulations) --Steank
     * March 7, 2021: Award revoked due to method doing more than previously written
     * R.I.P. updateRepresentingItemStackAfterShooting
     *
     * @param shots The number of shots the player made
     */
    protected void updateAfterShooting(int shots) {
        setAmmo(currentAmmo - shots);
        setClipAmmo(currentClipAmmo - shots);

        Player player = getPlayer();

        if (currentClipAmmo == 0) {
            if (currentAmmo > 0) {
                reload();
            } else {
                player.sendMessage(Component.text("You don't have any ammo!", NamedTextColor.RED));
            }
        }

        // Animate xp bar
        fireDelayTask = getArena().runTaskTimer(0L, 1L, new DisposableBukkitRunnable() {

            @SuppressWarnings("ConstantConditions")
            private final int goal = (int) Math.round(getCurrentLevel().getFireRate()
                    * getZombiesPlayer().getFireRateMultiplier().getValue());

            private final float stepVal = 1F / goal;

            private int step = 0;

            @Override
            public void run() {
                if (step++ < goal) {
                    if (isSelected()) {
                        player.setExp(step * stepVal);
                    }
                } else {
                    if (isSelected()) {
                        player.setExp(1);
                    }

                    if (canReload && currentAmmo > 0) {
                        canShoot = true;
                    }
                    fireDelayTask = -1;
                    cancel();
                }
            }

        }).getTaskId();

        Sound sound = getEquipmentData().getSound();

        Location playerLocation = player.getLocation();
        player.getWorld().playSound(sound, playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
    }

    /**
     * Utility method to set the item durability
     * @param val the damage value to set
     */
    private void setItemDamage(int val) {
        ItemStack itemStack = getRepresentingItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Damageable damageable) {
            damageable.setDamage(val);

            getRepresentingItemStack().setItemMeta(itemMeta);
            setRepresentingItemStack(getRepresentingItemStack());
        }
    }

    /**
     * Sets the ammo of the weapon
     * @param ammo The new ammo
     */
    public void setAmmo(int ammo) {
        currentAmmo = ammo;
        if (isVisible() && isSelected()) {
            updateAmmo();
        }
    }

    private void updateAmmo() {
        getPlayer().setLevel(currentAmmo);
    }

    /**
     * Sets the clip ammo of the weapon
     * @param clipAmmo The new clip ammo
     */
    public void setClipAmmo(int clipAmmo) {
        this.currentClipAmmo = clipAmmo;

        if (isVisible()) {
            updateClipAmmo();
        }
    }

    private void updateClipAmmo() {
        if (currentClipAmmo > 0) {
            setItemDamage(0);
            getRepresentingItemStack().setAmount(currentClipAmmo);
        } else {
            setItemDamage(getEquipmentData().getMaterial().getMaxDurability());
            getRepresentingItemStack().setAmount(1);
        }
        setRepresentingItemStack(getRepresentingItemStack());

        getPlayer().updateInventory();
    }

    @Override
    public void upgrade() {
        super.upgrade();
        refill();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            updateAmmo();
            updateClipAmmo();
        }

        super.setVisible(visible);
    }

    @Override
    public void onSlotSelected() {
        super.onSlotSelected();
        Player player = getPlayer();

        player.setLevel(currentAmmo);
        player.setExp(1);
    }

    @Override
    public void onSlotDeselected() {
        super.onSlotDeselected();
        Player player = getPlayer();

        player.setLevel(0);
        player.setExp(0);
    }

    @Override
    public void onLeftClick(@NotNull Action action) {
        super.onLeftClick(action);
        reload();
    }

    @Override
    public void onRightClick(@NotNull Action action) {
        super.onRightClick(action);

        if (canShoot) {
            canShoot = false;
            shoot(); // Shoot your first shot on the same tick

            if (getCurrentLevel().getShotsPerClick() > 1) { // Don't schedule if unnecessary
                shootingTask = getArena().runTaskTimer(1L, 1L, new DisposableBukkitRunnable() {

                    private int firedShots = 1;

                    @Override
                    public void run() {
                        if (firedShots++ == Math.min(getCurrentLevel().getShotsPerClick(), currentAmmo)) { // Reload while shooting possible
                            getArena().getStatsManager().queueCacheRequest(CacheInformation.PLAYER,
                                    getPlayer().getUniqueId(), PlayerGeneralStats::new,
                                    (stats) -> stats.setBulletsShot(stats.getBulletsShot() + firedShots));
                            updateAfterShooting(firedShots);

                            shootingTask = -1;
                            cancel();
                        }

                        shoot();
                    }

                }).getTaskId();
            } else {
                getArena().getStatsManager().queueCacheRequest(CacheInformation.PLAYER,
                        getPlayer().getUniqueId(), PlayerGeneralStats::new,
                        (stats) -> stats.setBulletsShot(stats.getBulletsShot() + 1));
                updateAfterShooting(1);
            }
        }
    }

    /**
     * Shoots the gun
     */
    public abstract void shoot();
}
