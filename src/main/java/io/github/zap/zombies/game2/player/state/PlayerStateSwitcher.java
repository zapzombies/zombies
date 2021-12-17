package io.github.zap.zombies.game2.player.state;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class PlayerStateSwitcher<E extends Enum<E>> {

    private final Map<E, PlayerState> states;

    private PlayerState state = null;

    public PlayerStateSwitcher(@NotNull Map<E, PlayerState> states) {
        this.states = Objects.requireNonNull(states);
    }

    public void setState(@NotNull E type) {
        PlayerState state = states.get(type);

        if (state == null) {
            throw new IllegalArgumentException("Type  " + type + " is not registered for this player switcher!");
        }

        this.state = state;
    }

    public void applyState(@NotNull Player player) {
        if (state == null) {
            throw new IllegalStateException("A state has not yet been chosen!");
        }

        state.apply(player);
    }

}
