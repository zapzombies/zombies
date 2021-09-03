package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.DamageModificationPowerUpData;

/**
 * Modify the damage dealt by players using a modifier function f(x) = x * multiplier
 */
@PowerUpType(name = "Damage-Modification")
public class DamageModificationPowerUp extends DurationPowerUp{
    public DamageModificationPowerUp(DamageModificationPowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public DamageModificationPowerUp(DamageModificationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }
}
