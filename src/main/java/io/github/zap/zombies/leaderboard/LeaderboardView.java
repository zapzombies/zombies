package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface LeaderboardView {

    void push(int index, @NotNull Component message);

    void displayToPlayer(@NotNull Player player);

    void destroy();

}
