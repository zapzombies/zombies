package io.github.zap.zombies.leaderboard;

import io.github.zap.arenaapi.hologram.Hologram;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class HologramLeaderboardView implements LeaderboardView {

    private final Hologram hologram;

    private final int startIndex;

    public HologramLeaderboardView(@NotNull Hologram hologram, int startIndex) {
        this.hologram = hologram;
        this.startIndex = startIndex;
    }

    @Override
    public void push(int index, @NotNull Component message) {
        hologram.updateLine(index + startIndex, message);
    }

    @Override
    public void displayToPlayer(@NotNull Player player) {
        hologram.renderToPlayer(player);
    }

    @Override
    public void destroy() {
        hologram.destroy();
    }

}
