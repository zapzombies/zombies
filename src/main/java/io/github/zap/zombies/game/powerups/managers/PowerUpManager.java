package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.PowerUpData;
import io.github.zap.zombies.game.data.powerups.spawnrules.SpawnRuleData;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * Manages loaded power ups
 */
public interface PowerUpManager {
    /**
     * Retrieve all power up data loaded by this PowerUpManager
     * @return a Set contains all power up data
     */
    Set<PowerUpData> getDataSet();

    /**
     * Add a power up data
     * @param data the data to add
     */
    void addPowerUpData(PowerUpData data);

    /**
     * Remove a power up data
     * @param name the name of the power up to remove
     */
    void removePowerUpsData(String name);

    /**
     * Get all the spawnrules data loaded by this PowerUpManager
     * @return a Set contains all spawnrule data
     */
    Set<SpawnRuleData> getSpawnRules();

    /**
     * Add a spawnrule data
     * @param spawnRuleData the spawnrule to add
     */
    void addSpawnRuleData(SpawnRuleData spawnRuleData);

    /**
     * Remove a spawnrule data
     * @param name the name of the spawnrule to remove
     */
    void removeSpawnRuleData(String name);

    /**
     * Retrieve all power up class initializers loaded by this PowerUpManager
     * @return a Set contains all power up class initializers
     */
    Set<Pair<BiFunction<PowerUpData,ZombiesArena, PowerUp>, Class<? extends PowerUpData>>> getPowerUpInitializers();

    /**
     * Register a power up type
     * @param name the name of the power up type
     * @param powerUpsInitializer the class initializer
     * @param dataClass the data this class accept
     */
    void registerPowerUp(String name, BiFunction<PowerUpData,ZombiesArena, PowerUp> powerUpsInitializer, Class<? extends PowerUpData> dataClass);

    /**
     * Register a power up type
     * @param name the name of the power up type
     * @param classType the power up type class
     */
    void registerPowerUp(String name, Class<? extends PowerUp> classType);

    /**
     * Register a power up type, name will be loaded by its annotation
     * @param classType The power up type class
     */
    void registerPowerUp(Class<? extends PowerUp> classType);

    /**
     * Unregister a power up type
     * @param name the name of the power up type
     */
    void unregisterPowerUp(String name);

    /**
     * Retrieve all spawnrule class initializers loaded by this PowerUpManager
     * @return a Set contains all spawnrule class initializers
     */
    Set<Pair<SpawnRuleCtor<?, ?>, Class<? extends SpawnRuleData>>> getSpawnRuleInitializers();

    /**
     * Register a spawnrule type
     * @param name the name of the spawnrule type
     * @param initializer the spawnrule class initializer
     * @param dataClass the dataclass this spawnrule type accepts
     */
    void registerSpawnRule(String name, SpawnRuleCtor<?, ?> initializer, Class<? extends SpawnRuleData> dataClass);

    /**
     * Register a spawnrule type
     * @param name the name of the spawnrule type
     * @param spawnRule the spawnrule type class
     */
    void registerSpawnRule(String name, Class<? extends PowerUpSpawnRule<?>> spawnRule);

    /**
     * Register a spawnrule type, name will be loaded by its annotation
     * @param spawnRule the spawnrule type class
     */
    void registerSpawnRule(Class<? extends PowerUpSpawnRule<?>> spawnRule);

    /**
     * Create a new power up instance
     * @param name the name of the power up
     * @param arena the owning arena
     * @return a new power up
     */
    PowerUp createPowerUp(String name, ZombiesArena arena);

    /**
     * Create a new power up spawnrule instance
     * @param name the name of the power up spawn rule
     * @param powerUpName the power up name that this spawnrule will spawn
     * @param arena the owning arena
     * @return a new power up spawnrule
     */
    PowerUpSpawnRule<?> createSpawnRule(String name, String powerUpName, ZombiesArena arena);
}
