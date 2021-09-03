package io.github.zap.zombies.game.powerups;

public enum PowerUpState {
    NONE, // When the power up object is created but not spawn item yet
    DROPPED, // When the power up object is placed into the arena
    ACTIVATED, // When the power up object is activated
    REMOVED // When the power up object is removed
}
