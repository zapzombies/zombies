package io.github.zap.zombies.game2.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface MessagePipe {

    void sendMessage(@NotNull Component message);

    static @NotNull MessagePipe chat(@NotNull BasePlayer player) {
        return message -> player.getPlayerIfValid().ifPresent(bukkitPlayer -> bukkitPlayer.sendMessage(message));
    }

    static @NotNull MessagePipe actionBar(@NotNull BasePlayer player) {
        return message -> player.getPlayerIfValid().ifPresent(bukkitPlayer -> bukkitPlayer.sendActionBar(message));
    }

}
