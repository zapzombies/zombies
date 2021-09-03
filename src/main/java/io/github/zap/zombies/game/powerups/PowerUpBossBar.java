package io.github.zap.zombies.game.powerups;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.DurationPowerUpData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

/**
 * This class displays information about activated power ups
 */
public class PowerUpBossBar implements Disposable, Runnable {
    final BossBar bukkitBossBar;
    final BukkitTask updateTask;
    final ZombiesArena arena;
    final DecimalFormat formatter;
    boolean isVisible;

    public PowerUpBossBar(ZombiesArena arena, int refreshRate) {
        this.arena = arena;
        bukkitBossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        updateTask = arena.runTaskTimer(0L, refreshRate, this);
        formatter = new DecimalFormat("##.#");
        arena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        arena.getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);
        arena.getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
        bukkitBossBar.setVisible(false);
    }

    private void onPlayerLeave(ManagingArena<ZombiesArena, ZombiesPlayer>.ManagedPlayerListArgs managedPlayerListArgs) {
        managedPlayerListArgs.getPlayers().forEach(x -> {
            if (x.getPlayer() != null) {
                bukkitBossBar.removePlayer(x.getPlayer());
            }
        });
    }

    private void onPlayerJoin(ManagingArena.PlayerListArgs playerListArgs) {
        playerListArgs.getPlayers().forEach(bukkitBossBar::addPlayer);
    }

    private void onPlayerRejoin(ZombiesArena.ManagedPlayerListArgs playerListArgs) {
        for (ZombiesPlayer player : playerListArgs.getPlayers()) {
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer != null) {
                bukkitBossBar.addPlayer(player.getPlayer());
            }
        }
    }

    @Override
    public void run() {
        var longest = findLongest();
        if(longest == null) {
            bukkitBossBar.setVisible(false);
            return;
        }


        var items = arena.getPowerUps().stream()
                .filter(x -> x instanceof DurationPowerUp && x.getState() == PowerUpState.ACTIVATED)
                .filter(x -> x != longest)
                .collect(Collectors.toSet());

        StringBuilder sb = new StringBuilder();
        sb.append(longest.getData().getDisplayName());
        items.stream().limit(3).forEach(x -> sb.append(ChatColor.RESET).append(ChatColor.GRAY).append(", ").append(x.getData().getDisplayName()));
        if(items.size() > 3) {
            sb.append(ChatColor.DARK_GRAY).append("...");
        }
        var millis = (longest.getEstimatedEndTimeStamp() - System.currentTimeMillis());
        sb.append(ChatColor.GRAY).append(" - ").append(formatter.format(millis / 1000f)).append(" seconds");

        if(!bukkitBossBar.isVisible()) bukkitBossBar.setVisible(true);
        bukkitBossBar.setTitle(sb.toString());
        bukkitBossBar.setColor(((DurationPowerUpData)longest.getData()).getBossBarColor());
        bukkitBossBar.setProgress(MathUtils.clamp(millis / 50f / (float)((DurationPowerUpData)longest.getData()).getDuration(), 0, 1));
    }

    private DurationPowerUp findLongest() {
        DurationPowerUp longest = null;

        for(var item : arena.getPowerUps()) {
            if(item instanceof DurationPowerUp && item.getState() == PowerUpState.ACTIVATED) {
                var current = (DurationPowerUp)item;
                if(longest == null || current.getEstimatedEndTimeStamp() > longest.getEstimatedEndTimeStamp()) {
                    longest = current;
                }
            }
        }
        return longest;
    }

    @Override
    public void dispose() {
        if(!updateTask.isCancelled())
            updateTask.cancel();

        arena.getPlayerJoinEvent().removeHandler(this::onPlayerJoin);
        arena.getPlayerLeaveEvent().removeHandler(this::onPlayerLeave);
        bukkitBossBar.removeAll();
    }
}
