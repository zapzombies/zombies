package io.github.zap.zombies.game;

import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something that can be a source of damage toward an ActiveMob (such as a player, or possibly the Arena)
 */
public interface Damager {
    /**
     * Called after damage is dealt to the specified ActiveMob.
     * @param item The item that was used to deal damage to the mob
     * @param damaged The mob that was damaged
     * @param deltaHealth The change (reduction) in mob health
     */
    default void onDealsDamage(@NotNull DamageAttempt item, @NotNull Mob damaged, double deltaHealth) {}
}
