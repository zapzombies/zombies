package io.github.zap.zombies.game2.player;

import io.github.zap.arenaapi.hotbar2.PlayerView;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class BasePlayer implements PlayerView {

    private final Server server;

    private final UUID playerUUID;

    private Player player = null;

    private int captureTick = -1;

    private boolean inGame = true;

    public BasePlayer(@NotNull Server server, @NotNull UUID playerUUID) {
        this.server = Objects.requireNonNull(server, "server cannot be null!");
        this.playerUUID = Objects.requireNonNull(playerUUID, "playerUUID cannot be null!");
    }

    @Override
    public @NotNull Optional<Player> getPlayerIfValid() {
        int currentTick = server.getCurrentTick();
        if (currentTick == captureTick) {
            return Optional.ofNullable(player);
        }

        captureTick = currentTick;
        return Optional.ofNullable(player = server.getPlayer(playerUUID));
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return getPlayerIfValid().map(OfflinePlayer.class::cast).orElseGet(() -> server.getOfflinePlayer(playerUUID));
    }

    @Override
    public @NotNull UUID getUUID() {
        return playerUUID;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

}
