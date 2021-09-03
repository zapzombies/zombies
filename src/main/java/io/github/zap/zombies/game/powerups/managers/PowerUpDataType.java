package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.data.powerups.*;
import lombok.Getter;

/**
 * All data type defined by zap dev team
 */
public enum PowerUpDataType implements PowerUpDataTypeLinker {
    BASIC("basic", PowerUpData.class),
    DURATION("duration", DurationPowerUpData.class),
    EARNED_GOLD_MOD("earned_gold_mod", EarnedGoldMultiplierPowerUpData.class),
    BARRICADE_COUNT_MOD("window_barricade_count_mod", BarricadeCountModificationPowerUpData.class),
    MULTIPLIER("multiplier", ModifierModificationPowerUpData.class),
    MULTIPLIER_WITH_MODE("multiplier+", ModifierModeModificationPowerUpData.class),
    DAMAGE_MOD("damage_mod", DamageModificationPowerUpData.class);

    @Getter
    private final String name;

    @Getter
    private final Class<? extends PowerUpData> dataType;

    PowerUpDataType(String name, Class<? extends PowerUpData> dataType) {
        this.name = name;
        this.dataType = dataType;
    }
}
