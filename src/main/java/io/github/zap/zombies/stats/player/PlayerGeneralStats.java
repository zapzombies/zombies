package io.github.zap.zombies.stats.player;

import io.github.zap.arenaapi.stats.Stats;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A player's overall Zombies stats
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerGeneralStats extends Stats<UUID> {

    public PlayerGeneralStats(@NotNull UUID uuid) {
        super(uuid);
    }

    @SuppressWarnings("unused")
    private PlayerGeneralStats() {

    }

    int bulletsShot = 0;

    int bulletsHit = 0;

    int headShots = 0;

    Map<String, PlayerMapStats> mapStatsMap = new HashMap<>();

    /**
     * Gets the map stats for a map
     * @param map The map to get stats for
     * @return The map stats
     */
    public @NotNull PlayerMapStats getMapStatsForMap(@NotNull MapData map) {
        return mapStatsMap.computeIfAbsent(map.getName(), (unused) -> new PlayerMapStats(map.getName()));
    }

}
