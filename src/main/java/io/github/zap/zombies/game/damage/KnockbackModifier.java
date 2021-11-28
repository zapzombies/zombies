package io.github.zap.zombies.game.damage;

import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface KnockbackModifier {

    double modify(@NotNull Mob target, double currentKnockback);

}
