package io.github.zap.zombies.game.powerups.spawnrules;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.spawnrules.SpawnRuleData;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.events.ChangedAction;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import lombok.Getter;
import org.bukkit.Location;

import java.util.Collections;

/**
 * This class or its subclasses create Power ups when a condition is met
 * @param <T>
 */
public abstract class PowerUpSpawnRule<T extends SpawnRuleData> {
    @Getter
    private final T data;

    @Getter
    private final ZombiesArena arena;

    @Getter
    private final Event<PowerUp> powerUpSpawned = new Event<>();

    @Getter
    private final String spawnTargetName;

    public PowerUpSpawnRule(String spawnTargetName, T data, ZombiesArena arena) {
        this.data = data;
        this.arena = arena;
        this.spawnTargetName = spawnTargetName;
    }

    /**
     * Determines whether the current round restrict power up (eg: Boss rounds)
     * @return Whether this round disables power up
     */
    public boolean isDisabledRound() {
        var disabledRounds = getArena().getMap().getDisablePowerUpRound();
        var currentRound = getArena().getMap().getCurrentRoundProperty().getValue(getArena());
        return disabledRounds.contains(currentRound);
    }

    /**
     * Spawn the targeted power up
     * @param loc the location to spawn
     */
    protected void spawn(Location loc) {
        var pu = getArena().getPowerUpManager().createPowerUp(getSpawnTargetName(), getArena());
        pu.spawnItem(loc);
        powerUpSpawned.callEvent(pu);
        var eventArgs = new PowerUpChangedEventArgs(ChangedAction.ADD, Collections.singleton(pu));
        getArena().getPowerUps().add(pu);
        getArena().getPowerUpChangedEvent().callEvent(eventArgs);
    }
}
