package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A visual representation of a {@link Leaderboard}.
 */
public interface LeaderboardView {

    /**
     * Updates a message in the leaderboard
     * @param index The index to update the message in
     * @param message The message to use
     */
    void push(int index, @NotNull Component message);

    /**
     * Renders the {@link Leaderboard} to a {@link  Player}
     * @param player The {@link Player} to render to
     */
    void displayToPlayer(@NotNull Player player);

    /**
     * Destroys this view
     */
    void destroy();

}
