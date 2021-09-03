package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarricadeCountModificationPowerUpData extends ModifierModeModificationPowerUpData {
    private boolean affectAll; // For performance: when set to true ignore affectedRange
    private double affectedRange;
    private int rewardGold;
}
