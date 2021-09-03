package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifierModificationPowerUpData extends PowerUpData {
    private double multiplier;
    private double amount;
}
