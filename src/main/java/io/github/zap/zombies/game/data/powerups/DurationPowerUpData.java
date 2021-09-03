package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.boss.BarColor;

/**
 * The base class for all power up has long lasting effects
 */
@Getter
@Setter
public class DurationPowerUpData extends PowerUpData {
    private int duration = 600; // In ticks

    private BarColor bossBarColor = BarColor.WHITE;
}
