package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DamageModificationPowerUpData extends DurationPowerUpData {
    private boolean isInstaKill;
    private double multiplier;
    private double additionalDamage;
}
