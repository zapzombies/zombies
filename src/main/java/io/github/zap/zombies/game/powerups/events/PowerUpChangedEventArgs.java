package io.github.zap.zombies.game.powerups.events;

import io.github.zap.zombies.game.powerups.PowerUp;
import lombok.Value;

import java.util.Set;

@Value
public class PowerUpChangedEventArgs {
    ChangedAction action;

    // ChangedAction.CLEAR will not contains affected items
    Set<PowerUp> affectedItems;
}
