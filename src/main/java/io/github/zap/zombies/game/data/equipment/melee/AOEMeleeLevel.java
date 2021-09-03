package io.github.zap.zombies.game.data.equipment.melee;

import lombok.Getter;

/**
 * Level of an AOE melee weapon
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class AOEMeleeLevel extends MeleeLevel {

    private int maxEntities = 5;

    private double range = 2.0;

}
