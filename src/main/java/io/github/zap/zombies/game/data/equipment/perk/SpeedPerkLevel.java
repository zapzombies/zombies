package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

/**
 * Level of the speed perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class SpeedPerkLevel extends PerkLevel {

    private int duration = 0;

    private int amplifier = 0;

}
