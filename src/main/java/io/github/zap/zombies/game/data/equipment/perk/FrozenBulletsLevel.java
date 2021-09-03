package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

/**
 * Level of the frozen bullets perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class FrozenBulletsLevel extends PerkLevel {

    private double reducedSpeed = 0.0D;

    private int duration = 20;

}
