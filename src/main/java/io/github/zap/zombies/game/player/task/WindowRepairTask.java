package io.github.zap.zombies.game.player.task;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.task.ZombiesTask;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Task to repair nearby windows
 */
public class WindowRepairTask extends ZombiesTask {

    private final ZombiesPlayer player;

    private boolean repairOn = false;

    private WindowData targetWindow = null;

    public WindowRepairTask(ZombiesPlayer player) {
        super(player.getArena(), 0L, player.getArena().getMap().getWindowRepairTicks());
        this.player = player;

        arena.getProxyFor(PlayerToggleSneakEvent.class).registerHandler(args -> {
            if (args.getEvent().getPlayer().equals(player.getPlayer())) {
                repairOn = args.getEvent().isSneaking();
            }
        });
    }

    @Override
    protected void execute() {
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            if (player.isAlive()) {
                if (targetWindow == null) { // our target window is null, so look for one to repair
                    lookForNewWindow(bukkitPlayer);
                    return;
                } else if (repairOn && targetWindow.inRange(bukkitPlayer.getLocation().toVector(),
                        arena.getMap().getWindowRepairRadiusSquared())) { // we already have a target window - make sure it's still in range
                    tryRepairWindow(targetWindow);
                    return;
                }
            }

            resetTargetWindow();
        }
    }

    private void lookForNewWindow(@NotNull Player bukkitPlayer) {
        MapData map = arena.getMap();
        WindowData window = map.windowMatching(this::isWindowRepairable);

        if (window != null) {
            if (repairOn) {
                targetWindow = window;
                tryRepairWindow(targetWindow); // directly repair window; no need to perform checks
                bukkitPlayer.sendActionBar(Component.empty());
            } else {
                bukkitPlayer.sendActionBar(Component.text("Hold SHIFT to repair!",
                        NamedTextColor.YELLOW));
            }
        } else {
            bukkitPlayer.sendActionBar(Component.empty());
        }
    }

    private void resetTargetWindow() {
        if (targetWindow != null) {
            Property<ZombiesPlayer> repairingPlayerProperty = targetWindow.getRepairingPlayerProperty();
            if (repairingPlayerProperty.getValue(arena) == player) {
                repairingPlayerProperty.setValue(arena, null);
            }
        }

        targetWindow = null;
    }

    private boolean isWindowRepairable(WindowData windowData) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            return !windowData.isFullyRepaired(arena)
                    && windowData.getRepairingPlayerProperty().getValue(arena) == null
                    && windowData.inRange(bukkitPlayer.getLocation().toVector(),
                    arena.getMap().getWindowRepairRadiusSquared());
        } else {
            return false;
        }
    }

    private void tryRepairWindow(@NotNull WindowData targetWindow) {
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            Property<Entity> attackingEntityProperty = targetWindow.getAttackingEntityProperty();
            Entity attacker = attackingEntityProperty.getValue(arena);

            if (attacker == null || attacker.isDead()) {
                attackingEntityProperty.setValue(arena, null);

                if (getCurrentRepairer() == player) {
                    advanceRepairState(bukkitPlayer);
                } else {
                    bukkitPlayer.sendMessage(Component.text("Someone is already repairing that window!",
                            NamedTextColor.RED));
                }
            } else {
                bukkitPlayer.sendMessage(Component.text("A mob is attacking that window!", NamedTextColor.RED));
            }
        }
    }

    private @NotNull ZombiesPlayer getCurrentRepairer() {
        Property<ZombiesPlayer> currentRepairerProperty = targetWindow.getRepairingPlayerProperty();
        ZombiesPlayer currentRepairer = currentRepairerProperty.getValue(arena);

        if (currentRepairer == null || !currentRepairer.isAlive()) {
            currentRepairer = player;
            currentRepairerProperty.setValue(arena, player);
        }

        return currentRepairer;
    }

    private void advanceRepairState(@NotNull Player bukkitPlayer) {
        int previousIndex = targetWindow.getCurrentIndexProperty().getValue(arena);
        int blocksRepaired = targetWindow.advanceRepairState(arena, player.getRepairIncrement());
        for (int i = previousIndex; i < previousIndex + blocksRepaired; i++) {
            Block target = WorldUtils.getBlockAt(arena.getWorld(), targetWindow.getFaceVectors().get(i + 1));
            target.setBlockData(Bukkit.createBlockData(targetWindow.getRepairedData().get(i + 1)));
            playRepairSound(bukkitPlayer, i);
        }

        player.addCoins(blocksRepaired * arena.getMap().getCoinsOnRepair());
    }

    private void playRepairSound(@NotNull Player bukkitPlayer, int index) {
        Vector center = targetWindow.getCenter();

        if (index < targetWindow.getVolume() - 2) {
            arena.getWorld().playSound(targetWindow.getBlockRepairSound(), center.getX(), center.getY(), center.getZ());
        } else {
            updateStats(bukkitPlayer);
            arena.getWorld().playSound(targetWindow.getWindowRepairSound(), center.getX(), center.getY(),
                    center.getZ());
        }
    }

    private void updateStats(@NotNull Player bukkitPlayer) {
        arena.getStatsManager().queueCacheRequest(CacheInformation.PLAYER, bukkitPlayer.getUniqueId(),
                PlayerGeneralStats::new, (stats) -> {
            PlayerMapStats mapStats = stats.getMapStatsForMap(arena.getMap());
            mapStats.setWindowsRepaired(mapStats.getWindowsRepaired() + 1);
        });
    }

    @Override
    public void notifyChange() {
        super.notifyChange();
        if (player.isAlive()) {
            start();
        } else {
            stop();
        }
    }

    @Override
    public void stop() {
        super.stop();
        resetTargetWindow();
    }

}
