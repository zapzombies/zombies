package io.github.zap.zombies.game.player.task;

import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.task.ZombiesTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Task to ensure players are within bounds
 */
public class BoundsCheckTask extends ZombiesTask {

    private final ZombiesPlayer player;

    public BoundsCheckTask(@NotNull ZombiesPlayer player) {
        super(player.getArena(), 0L, 5L);
        this.player = player;
    }

    @Override
    protected void execute() {
        MapData map = arena.getMap();
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            Location playerLocation = bukkitPlayer.getLocation();
            Vector playerLocationVector = playerLocation.toVector();
            RoomData roomIn = map.roomAt(playerLocationVector);

            if (roomIn != null) {
                for (WindowData windowData : roomIn.getWindows()) {
                    if (windowData.playerInside(playerLocationVector)) {
                        Vector target = windowData.getTarget();
                        bukkitPlayer.teleportAsync(new Location(arena.getWorld(), target.getX(), target.getY(),
                                target.getZ(), playerLocation.getYaw(), playerLocation.getPitch()));

                        break;
                    }
                }
            }
        }
    }

}
