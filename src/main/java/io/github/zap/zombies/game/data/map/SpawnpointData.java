package io.github.zap.zombies.game.data.map;

import io.github.zap.zombies.Zombies;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.util.Vector;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpawnpointData {
    /**
     * The location of this spawnpoint
     */
    Vector spawn;

    /**
     * Used to retrieve an object that defines the behavior of the spawnpoint (what mobs it can spawn, what mobs it
     * can't spawn, etc)
     */
    String ruleName;

    @SuppressWarnings("unused")
    private SpawnpointData() {}

    public boolean canSpawn(String mob, MapData map) {
        SpawnRule rule = map.getSpawnRules().get(ruleName);

        if(rule != null) {
            if(rule.isBlacklist()) {
                return !rule.getMobSet().contains(mob);
            }
            else {
                return rule.getMobSet().contains(mob);
            }
        }
        else {
            Zombies.warning(String.format("SpawnRule %s does not exist. Allowing mob to spawn.", ruleName));
            return true;
        }
    }
}
