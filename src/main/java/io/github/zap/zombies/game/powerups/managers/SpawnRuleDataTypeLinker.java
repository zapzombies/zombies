package io.github.zap.zombies.game.powerups.managers;


import io.github.zap.zombies.game.data.powerups.spawnrules.SpawnRuleData;

public interface SpawnRuleDataTypeLinker {
    String getName();
    Class<? extends SpawnRuleData> getDataType();
}
