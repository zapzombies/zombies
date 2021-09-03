package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

/**
 * A level of the extra health perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class ExtraHealthLevel extends PerkLevel {

    private int additionalHealth = 0;

}
