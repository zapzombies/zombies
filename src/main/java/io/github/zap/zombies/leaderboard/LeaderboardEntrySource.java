package io.github.zap.zombies.leaderboard;

import org.jetbrains.annotations.NotNull;

/**
 * A source of {@link LeaderboardEntry}s
 */
public interface LeaderboardEntrySource {

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
    @NotNull LeaderboardEntry getEntry(int index);

}
