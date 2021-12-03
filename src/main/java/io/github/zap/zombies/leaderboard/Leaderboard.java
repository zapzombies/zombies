package io.github.zap.zombies.leaderboard;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class Leaderboard {

    private final LeaderboardLineSource source;

    private final LeaderboardView view;

    public Leaderboard(@NotNull LeaderboardLineSource source, @NotNull LeaderboardView view) {
        this.source = source;
        this.view = view;
    }

    public void displayToPlayer(@NotNull Player player) {
        view.displayToPlayer(player);
    }

    public void update(int index) {
        if (index < source.count()) {
            view.push(index, source.getEntry(index));
        }
    }

    public void updateAll() {
        for (int i = 0; i < source.count(); i++) {
            view.push(i, source.getEntry(i));
        }
    }

    public void destroy() {
        view.destroy();
    }

}
