package io.github.zap.zombies.leaderboard;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A leaderboard which represents Zombies player stats
 */
@SuppressWarnings("ClassCanBeRecord")
public class Leaderboard {

    private final LeaderboardLineSource source;

    private final LeaderboardView view;

    /**
     * Creates a leaderboard
     * @param source A {@link LeaderboardLineSource} for the lines of the leaderboard
     * @param view A {@link LeaderboardView} to display the leaderboard with
     */
    public Leaderboard(@NotNull LeaderboardLineSource source, @NotNull LeaderboardView view) {
        this.source = source;
        this.view = view;
    }

    /**
     * Displays the leaderboard to a {@link Player}
     * @param player The {@link Player} to display to
     */
    public void displayToPlayer(@NotNull Player player) {
        view.displayToPlayer(player);
    }

    /**
     * Updates a single line in the leaderboard
     * @param index The line to update
     */
    public void update(int index) {
        if (index < source.count()) {
            view.push(index, source.getEntry(index));
        }
    }

    /**
     * Refreshes all lines in the leaderboard
     */
    public void updateAll() {
        for (int i = 0; i < source.count(); i++) {
            view.push(i, source.getEntry(i));
        }
    }

    /**
     * Destroys the leaderboard
     */
    public void destroy() {
        view.destroy();
    }

}
