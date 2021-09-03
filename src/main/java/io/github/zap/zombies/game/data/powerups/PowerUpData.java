package io.github.zap.zombies.game.data.powerups;

import io.github.zap.zombies.game.data.util.ItemStackDescription;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;

/**
 * The base class of all power up data
 */
@Getter
@Setter
public class PowerUpData {
    private String type;
    private String name;
    private String powerUpType;

    private ItemStackDescription itemRepresentation;
    private String displayName;

    // In ticks
    private int despawnDuration = 6000;
    private double pickupRange = 1;

    private Sound pickupSound = Sound.ENTITY_ITEM_PICKUP;

    private float pickupSoundVolume = 1;

    private float pickupSoundPitch = 1;
}
