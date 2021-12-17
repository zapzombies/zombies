package io.github.zap.zombies.game2.arena.cleanup;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PlayerCleanup {

    void apply(@NotNull Player player);

}
