package io.github.zap.zombies.game.powerups.managers;

import lombok.Value;

/**
 * Represent options for instantiating JacksonPowerUpManager
 */
@Value
public class JacksonPowerUpManagerOptions {
    /**
     * Loads default power ups mode by zap team
     */
    boolean loadDefaults = true;
}
