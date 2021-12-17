package io.github.zap.zombies.game2.player.state;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class PlayerState {

    private final Collection<Condition> conditions;

    public PlayerState(@NotNull Collection<Condition> conditions) {
        this.conditions = Objects.requireNonNull(conditions, "conditions cannot be null!");
    }

    public void apply(@NotNull Player player) {
        for (Condition condition : conditions) {
            condition.apply(player);
        }
    }

}
