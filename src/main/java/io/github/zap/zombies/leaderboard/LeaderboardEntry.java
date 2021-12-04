package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A basic entry in a leaderboard
 */
public interface LeaderboardEntry {

    /**
     * The player that achieved the leaderboard position
     * @return A {@link Component} representing the player's name (implementations need not make this constant)
     */
    @NotNull Component player();

    /**
     * A {@link Component} representation of the value of the leaderboard position
     * @return The value representation
     */
    @NotNull Component value();

}
