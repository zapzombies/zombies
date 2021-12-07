package io.github.zap.zombies.game;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public interface HiddenPlayerSet extends Iterable<UUID> {

    void addPlayer(@NotNull UUID playerUUID);

    boolean hasPlayer(@NotNull UUID playerUUID);

    void removePlayer(@NotNull UUID playerUUID);

    static @NotNull HiddenPlayerSet basic(@NotNull Plugin plugin, @NotNull World world) {
        return new HiddenPlayerSet() {

            @NotNull
            @Override
            public Iterator<UUID> iterator() {
                return players.iterator();
            }

            private final Set<UUID> players = new HashSet<>();

            @Override
            public void addPlayer(@NotNull UUID playerUUID) {
                if (players.add(playerUUID)) {
                    for (Player player : world.getPlayers()) {
                        player.hidePlayer(plugin, player);
                    }
                }
            }

            @Override
            public boolean hasPlayer(@NotNull UUID playerUUID) {
                return players.contains(playerUUID);
            }

            @Override
            public void removePlayer(@NotNull UUID playerUUID) {
                if (players.remove(playerUUID)) {
                    for (Player player : world.getPlayers()) {
                        player.showPlayer(plugin, player);
                    }
                }
            }
        };
    }

}
