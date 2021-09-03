package io.github.zap.zombies.game.powerups.managers;


import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.spawnrules.SpawnRuleData;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;

@FunctionalInterface
public interface SpawnRuleCtor<D extends SpawnRuleData, T extends PowerUpSpawnRule<D>> {
    T construct(String name, SpawnRuleData data, ZombiesArena arena);
}
