package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class BasicLeaderboardLineSource implements LeaderboardLineSource {

    private final LeaderboardEntrySource source;

    private final LeaderboardLineCreator lineCreator;

    public BasicLeaderboardLineSource(@NotNull LeaderboardEntrySource source,
                                      @NotNull LeaderboardLineCreator lineCreator) {
        this.source = source;
        this.lineCreator = lineCreator;
    }

    @Override
    public int count() {
        return source.count();
    }

    @Override
    public @NotNull Component getEntry(int index) {
        return lineCreator.create(index, source.getEntry(index));
    }

}
