package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A source of {@link Component}s for leaderboards
 */
public interface LeaderboardLineSource {

    /**
     * Gets the number of lines in the source
     * @return The number of lines
     */
    int count();

    /**
     * Gets an entry
     * @param index The index of the entry
     * @return The respective entry
     */
    @NotNull Component getEntry(int index);

}
