package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.data.powerups.PowerUpData;

/**
 * Link data class with a associated name
 */
public interface PowerUpDataTypeLinker {
    String getName();
    Class<? extends PowerUpData> getDataType();

}
