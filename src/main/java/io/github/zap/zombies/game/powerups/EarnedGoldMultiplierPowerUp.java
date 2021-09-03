package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.EarnedGoldMultiplierPowerUpData;

// Although this type is empty but we still need the annotation and
// it is more convenient for us to use instanceof rather than checking the
// underlying data

/**
 * Modify the amount of gold eared using a modifier function f(x) = x * multiplier
 */
@PowerUpType(name = "Earned-Gold-Multiplier")
public class EarnedGoldMultiplierPowerUp extends DurationPowerUp {
    public EarnedGoldMultiplierPowerUp(EarnedGoldMultiplierPowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public EarnedGoldMultiplierPowerUp(EarnedGoldMultiplierPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }
}
