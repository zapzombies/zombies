package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface LeaderboardLineSource {

    int count();

    @NotNull Component getEntry(int index);

}
