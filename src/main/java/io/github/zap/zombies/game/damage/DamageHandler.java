package io.github.zap.zombies.game.damage;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * General interface for an implementation that handles damaging all entities.
 */
public interface DamageHandler {
    /**
     * Damages an entity.
     * @param target The ActiveMob to damage
     */
    void damageEntity(@NotNull Damager comesFrom, @NotNull DamageAttempt with, @NotNull Mob target);
}
