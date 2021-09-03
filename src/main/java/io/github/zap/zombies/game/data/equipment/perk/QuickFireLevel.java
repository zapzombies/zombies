package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

/**
 * Level of the quick fire perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class QuickFireLevel extends PerkLevel {

    private double multiplier = 0.8D;

}
