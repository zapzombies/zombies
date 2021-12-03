package io.github.zap.zombies.leaderboard;

import org.jetbrains.annotations.NotNull;

public interface LeaderboardEntrySource {

    int count();

    @NotNull LeaderboardEntry getEntry(int index);

}
