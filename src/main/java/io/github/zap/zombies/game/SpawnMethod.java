package io.github.zap.zombies.game;

/**
 * Used to control how mobs are spawned.
 */
public enum SpawnMethod {
    /**
     * Mobs will only spawn within the map-specified distance of any player (SLA) and not in closed rooms
     */
    RANGED,

    /**
     * Mobs will spawn regardless of player distance (not sla) and not in closed rooms
     */
    RANGELESS,

    /**
     * Mobs will spawn even in rooms that are closed, regardless of player distance
     */
    FORCE,

    /**
     * Mobs spawn with SLA and not in closed rooms, but ignore spawnpoint spawnrules
     */
    IGNORE_SPAWNRULE
}
