package io.github.zap.zombies.game.data.powerups.spawnrules;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DefaultPowerUpSpawnRuleData extends SpawnRuleData {
    private Set<Integer> pattern;

    private Set<Integer> waves;
}
