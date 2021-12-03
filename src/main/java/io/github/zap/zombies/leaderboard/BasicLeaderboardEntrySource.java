package io.github.zap.zombies.leaderboard;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class BasicLeaderboardEntrySource implements LeaderboardEntrySource {

    private final List<LeaderboardEntry> entries;

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
