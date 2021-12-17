package io.github.zap.zombies.game2.player;

import io.github.zap.arenaapi.hotbar2.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerList<P extends PlayerView> {

    private final Map<UUID, P> players = new HashMap<>();

    public @NotNull Map<UUID, P> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public void addPlayer(@NotNull P player) {
        players.put(player.getUUID(), player);
    }

    public void removePlayer(@NotNull UUID uuid) {
        players.remove(uuid);
    }

}
