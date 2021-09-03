package io.github.zap.zombies.stats.player;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

/**
 * A player's stats pertinent to a specific Zombies map
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerMapStats {

    public PlayerMapStats(String mapName) {
        this.mapName = mapName;
    }

    private PlayerMapStats() {

    }

    String mapName;

    int wins = 0;

    int knockDowns = 0;

    int deaths = 0;

    int kills = 0;

    int roundsSurvived = 0;

    int bestRound = 0;

    int doorsOpened = 0;

    int windowsRepaired = 0;

    int playersRevived = 0;

    int timesPlayed = 0;

    Long bestTime = null;

    Map<Integer, Long> bestTimes = new HashMap<>();

}
