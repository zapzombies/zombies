package io.github.zap.zombies.leaderboard;

import io.github.zap.arenaapi.hologram.Hologram;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of a {@link LeaderboardView} using {@link Hologram}s
 */
@SuppressWarnings("ClassCanBeRecord")
public class HologramLeaderboardView implements LeaderboardView {

    private final Hologram hologram;

    private final int startIndex;


    /**
     * Creates a {@link LeaderboardView} by using a {@link Hologram}
     * @param hologram The {@link Hologram} to display lines with
     * @param startIndex The index of the first leaderboard entry
     */
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
