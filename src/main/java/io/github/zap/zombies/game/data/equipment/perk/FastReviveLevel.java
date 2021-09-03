package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

/**
 * Level of the fast revive perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class FastReviveLevel extends PerkLevel {

    private int reducedReviveTime = 0;

}
