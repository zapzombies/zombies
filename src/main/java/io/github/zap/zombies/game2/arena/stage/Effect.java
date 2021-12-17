package io.github.zap.zombies.game2.arena.stage;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Effect {

    void apply(@NotNull Player player);

    static @NotNull Effect title(@NotNull Title title) {
        return player -> player.showTitle(title);
    }

    static @NotNull Effect sound(@NotNull Sound sound) {
        return player -> player.playSound(sound);
    }

    // TODO: MessagePipe?

}
