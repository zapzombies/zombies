package io.github.zap.zombies.game.data.map;

import io.github.zap.zombies.game.SpawnMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaveData {
    /**
     * time, in server ticks, before the wave's mobs are spawned
     */
    int waveLength = 100;

    /**
     * A list of the mobs that should be spawned during this wave
     */
    List<SpawnEntryData> spawnEntries = new ArrayList<>();

    /**
     * Controls the behavior of the spawner for this wave
     */
    SpawnMethod method = SpawnMethod.RANGED;

    /**
     * Used by SpawnMethod.RANGED
     */
    int slaSquared = 4096;

    /**
     * Whether this wave's mobs should be randomly distributed through the available spawnpoints.
     */
    boolean randomizeSpawnpoints = true;

    public WaveData() {}
}
