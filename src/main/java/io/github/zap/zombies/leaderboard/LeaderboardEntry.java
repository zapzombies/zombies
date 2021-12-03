package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface LeaderboardEntry {

    @NotNull Component player();

    @NotNull Component value();

}
