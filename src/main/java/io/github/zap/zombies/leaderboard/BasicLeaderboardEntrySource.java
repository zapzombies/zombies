package io.github.zap.zombies.leaderboard;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Basic implementation of a {@link LeaderboardEntrySource}
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicLeaderboardEntrySource implements LeaderboardEntrySource {

    private final List<LeaderboardEntry> entries;

    /**
     * Creates a basic entry source
     * @param entries A {@link List} of {@link LeaderboardEntry}s to wrap
     */
    public BasicLeaderboardEntrySource(@NotNull List<LeaderboardEntry> entries) {
        this.entries = entries;
    }

    @Override
    public int count() {
        return entries.size();
    }

    @Override
    public @NotNull LeaderboardEntry getEntry(int index) {
        return entries.get(index);
    }

}
