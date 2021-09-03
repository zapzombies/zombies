package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.DurationPowerUpData;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

/**
 * Base class for all power ups that have long lasting effect
 */
@PowerUpType(name = "Duration")
public class DurationPowerUp extends PowerUp {
    @Getter
    private long estimatedEndTimeStamp;

    private BukkitTask timeoutTask;

    public DurationPowerUp(DurationPowerUpData data, ZombiesArena arena) {
        super(data, arena);
    }

    public DurationPowerUp(DurationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    @Override
    public void activate() {
        restartTimeoutTimer();
    }

    /**
     * Reset the activated duration
     */
    protected void restartTimeoutTimer() {
        if(getState() != PowerUpState.ACTIVATED)
            throw new IllegalStateException("The perk must be activated to call this method!");

        stopTimeoutTimer();
        var duration = ((DurationPowerUpData)getData()).getDuration();

        timeoutTask = getArena().runTaskLater(duration, () -> {
            if(getState() == PowerUpState.ACTIVATED) {
                deactivate();
            }
        });

        estimatedEndTimeStamp = System.currentTimeMillis() + duration * 50L;
    }

    /**
     * Cancel the task which deactivate the power up
     */
    protected void stopTimeoutTimer() {
        if(timeoutTask != null && !timeoutTask.isCancelled()) {
            timeoutTask.cancel();
        }
    }
}
