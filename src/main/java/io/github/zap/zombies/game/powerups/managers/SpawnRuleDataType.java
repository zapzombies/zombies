package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.zombies.game.data.powerups.spawnrules.DefaultPowerUpSpawnRuleData;
import io.github.zap.zombies.game.data.powerups.spawnrules.SpawnRuleData;
import lombok.Getter;

public enum SpawnRuleDataType implements SpawnRuleDataTypeLinker {
    BASIC("basic", SpawnRuleData.class),
    DEFAULT("default", DefaultPowerUpSpawnRuleData .class);



    @Getter
    private final String name;

    @Getter
    private final Class<? extends SpawnRuleData> dataType;

    SpawnRuleDataType(String name, Class<? extends SpawnRuleData> dataType) {
        this.name = name;
        this.dataType = dataType;
    }
}
