package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EarnedGoldMultiplierPowerUpData extends DurationPowerUpData {
    private double multiplier;
}
