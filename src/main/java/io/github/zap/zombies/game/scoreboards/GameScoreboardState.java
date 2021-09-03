package io.github.zap.zombies.game.scoreboards;

import io.github.zap.zombies.game.ZombiesArenaState;

public interface GameScoreboardState {
    /**
     * Called when the arena change its state
     * @param gameState the previous ZombieArena's state. Null indicates that this instance is the initial state
     */
    void stateChangedFrom(ZombiesArenaState gameState, GameScoreboard scoreboard);

    /**
     * Update the scoreboard
     */
    void update();
}
