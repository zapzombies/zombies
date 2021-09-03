package io.github.zap.zombies.game.powerups.managers;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.FieldTypeDeserializer;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.Validate;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.PowerUpData;
import io.github.zap.zombies.game.data.powerups.spawnrules.SpawnRuleData;
import io.github.zap.zombies.game.powerups.*;
import io.github.zap.zombies.game.powerups.spawnrules.DefaultPowerUpSpawnRule;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import io.github.zap.zombies.game.powerups.spawnrules.SpawnRuleType;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Level;

/**
 * Manages loaded power ups that use json format
 * order of add/register: Power-up type -> Power-up / spawn rule type -> spawn rule
 */
public class JacksonPowerUpManager implements PowerUpManager, SupportEagerLoading {
    private final FieldTypeDeserializer<PowerUpData> powerUpDataDeserializer = new FieldTypeDeserializer<>("type");
    private final FieldTypeDeserializer<SpawnRuleData> spawnRuleDataFieldTypeDeserializer = new FieldTypeDeserializer<>("type");
    private final Map<String, PowerUpData> dataMap = new HashMap<>();
    private final Map<String, Pair<BiFunction<PowerUpData,ZombiesArena, PowerUp>, Class<? extends PowerUpData>>> typeMap = new HashMap<>();
    private final Map<String, SpawnRuleData> spawnRuleDataMap = new HashMap<>();
    private final Map<String, Pair<SpawnRuleCtor<?, ?>, Class<? extends SpawnRuleData>>> spawnRuleTypeMap = new HashMap<>();

    @Getter
    private boolean loaded;

    private boolean isLoading;

    @Getter
    private final DataLoader dataLoader;

    @Getter
    private final JacksonPowerUpManagerOptions options;

    public JacksonPowerUpManager(DataLoader dataLoader, JacksonPowerUpManagerOptions options) {
        this.dataLoader = dataLoader;
        this.options = options;
        ArenaApi.getInstance().addDeserializer(PowerUpData.class, powerUpDataDeserializer);
        ArenaApi.getInstance().addDeserializer(SpawnRuleData.class, spawnRuleDataFieldTypeDeserializer);
        addDataLoader(PowerUpDataType.BASIC);
        addSpawnRuleDataLoader(SpawnRuleDataType.BASIC);
        loaded = !options.isLoadDefaults();
    }

    public void addDataLoader(PowerUpDataTypeLinker dataTypeLinker) {
        powerUpDataDeserializer.getMappings().putIfAbsent(dataTypeLinker.getName(), dataTypeLinker.getDataType());
    }

    public void addSpawnRuleDataLoader(SpawnRuleDataTypeLinker dataTypeLinker) {
        spawnRuleDataFieldTypeDeserializer.getMappings().putIfAbsent(dataTypeLinker.getName(), dataTypeLinker.getDataType());
    }


    @Override
    public Set<PowerUpData> getDataSet() {
        ensureLoad();
        return new HashSet<>(dataMap.values());
    }

    @Override
    public void addPowerUpData(PowerUpData data) {
        addPowerUpData(data, true);
    }

    public void addPowerUpData(PowerUpData data, boolean throwOnError) {
        try {
            ensureLoad();
            Validate.isTrue(!dataMap.containsKey(data.getName()), data.getName() + " is already exist!");
            Validate.isTrue(typeMap.containsKey(data.getPowerUpType()), "Cannot find type: " + data.getPowerUpType() + "! Make sure you register power-up type before adding data.");
            Validate.isAssignableFrom(typeMap.get(data.getPowerUpType()).getRight(), data.getClass(), "The provided power up type does not accept this data type");
            dataMap.put(data.getName(), data);
        } catch (IllegalArgumentException e) {
            if(throwOnError) {
                throw e;
            } else {
                Zombies.log(Level.WARNING, "Error while loading power up data!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removePowerUpsData(String name) {
        ensureLoad();
        dataMap.remove(name);
    }

    @Override
    public Set<SpawnRuleData> getSpawnRules() {
        ensureLoad();
        return new HashSet<>(spawnRuleDataMap.values());
    }

    @Override
    public void addSpawnRuleData(SpawnRuleData spawnRuleData) {
        addSpawnRuleData(spawnRuleData, true);
    }

    public void addSpawnRuleData(SpawnRuleData spawnRuleData, boolean throwOnError) {
        try {
            ensureLoad();
            Validate.isTrue(!spawnRuleDataMap.containsKey(spawnRuleData.getName()), spawnRuleData.getName() + " Spawn rule already exist!");
            Validate.isTrue(spawnRuleTypeMap.containsKey(spawnRuleData.getSpawnRuleType()), spawnRuleData.getSpawnRuleType() + " Spawn rule type does not exist!");
            Validate.isAssignableFrom(spawnRuleTypeMap.get(spawnRuleData.getSpawnRuleType()).getRight(), spawnRuleData.getClass(), "The provided spawn rule type doesn't accept this data type!");
            spawnRuleDataMap.put(spawnRuleData.getName(), spawnRuleData);
        } catch (Exception e) {
            if (throwOnError) {
                throw e;
            } else {
                Zombies.log(Level.WARNING, "Error while loading power up data!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeSpawnRuleData(String name) {
        ensureLoad();
        spawnRuleDataMap.remove(name);
    }

    @Override
    public Set<Pair<BiFunction<PowerUpData,ZombiesArena, PowerUp>, Class<? extends PowerUpData>>> getPowerUpInitializers() {
        ensureLoad();
        return new HashSet<>(typeMap.values());
    }

    @Override
    public void registerPowerUp(String name, BiFunction<PowerUpData,ZombiesArena, PowerUp> powerUpsInitializer, Class<? extends PowerUpData> dataClass) {
        ensureLoad();
        Validate.isTrue(!typeMap.containsKey(name), name + " is already exist!");
        typeMap.put(name, Pair.of(powerUpsInitializer, dataClass));
    }

    @Override
    public void registerPowerUp(String name, Class<? extends PowerUp> classType) {
        BiFunction<PowerUpData,ZombiesArena, PowerUp> initializer = null;
        Class<? extends PowerUpData> dataClass = null;

        if(!typeMap.containsKey(name)) {
            for (Constructor<?> ctor : classType.getConstructors()) {
                var params = ctor.getParameterTypes();
                if(params.length == 2 && PowerUpData.class.isAssignableFrom(params[0]) && ZombiesArena.class.isAssignableFrom(params[1])) {
                    initializer = (data, arena) -> {
                        try {
                            return (PowerUp) ctor.newInstance(data, arena);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            Zombies.log(Level.SEVERE, "Unable to create power up type: " + name);
                            return null;
                        }
                    };

                    //noinspection unchecked
                    dataClass = (Class<? extends PowerUpData>) params[0];
                    break;
                }
            }
        } else {
            throw new IllegalArgumentException(name + " is already exist!");
        }

        if(initializer != null) {
            registerPowerUp(name, initializer, dataClass);
        } else {
            throw new IllegalArgumentException("Cannot find suitable constructor for the provided type");
        }
    }

    @Override
    public void registerPowerUp(Class<? extends PowerUp> classType) {
        for (var at : classType.getAnnotations()) {
            if (at instanceof PowerUpType powerUpType) {
                registerPowerUp(powerUpType.name(), classType);
                return;
            }
        }

        throw new IllegalArgumentException(classType + " does not have a defined @" + PowerUpType.class);
    }

    @Override
    public void unregisterPowerUp(String name) {
        ensureLoad();
        typeMap.remove(name);
        if(dataMap.values().stream().anyMatch(x -> x.getPowerUpType().equals(name))) {
            throw new IllegalStateException("Cannot remove power up type " + name + " because some power up data still depend on it!");
        }
    }

    @Override
    public Set<Pair<SpawnRuleCtor<?, ?>, Class<? extends SpawnRuleData>>> getSpawnRuleInitializers() {
        return new HashSet<>(spawnRuleTypeMap.values());
    }

    @Override
    public void registerSpawnRule(String name, SpawnRuleCtor<?, ?> initializer, Class<? extends SpawnRuleData> dataClass) {
        ensureLoad();
        Validate.isTrue(!spawnRuleTypeMap.containsKey(name), name + " is already exist!");
        spawnRuleTypeMap.put(name, Pair.of(initializer, dataClass));
    }

    @Override
    public void registerSpawnRule(String name, Class<? extends PowerUpSpawnRule<?>> spawnRuleType) {
        SpawnRuleCtor<?, ?> initializer = null;
        Class<? extends SpawnRuleData> dataClass = null;
        if(!spawnRuleTypeMap.containsKey(name)) {
            for(Constructor<?> ctor : spawnRuleType.getConstructors()) {
                var params = ctor.getParameterTypes();
                if(params.length == 3 && params[0] == String.class && SpawnRuleData.class.isAssignableFrom(params[1]) && ZombiesArena.class.isAssignableFrom(params[2])) {
                    initializer = (SpawnRuleCtor<SpawnRuleData, PowerUpSpawnRule<SpawnRuleData>>) (name1, data, arena) -> {
                        try {
                            //noinspection unchecked
                            return (PowerUpSpawnRule<SpawnRuleData>) ctor.newInstance(name1, data, arena);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            Zombies.log(Level.SEVERE, "Unable to create spawn rule type: " + name1);
                            return null;
                        }
                    };
                    //noinspection unchecked
                    dataClass = (Class<? extends SpawnRuleData>) params[1];
                    break;
                }
            }
        } else {
            throw new IllegalArgumentException(name + " is already exist!");
        }

        if(initializer != null) {
            registerSpawnRule(name, initializer, dataClass);
        } else {
            throw new IllegalArgumentException("Cannot find suitable constructor for the provided type");
        }
    }

    @Override
    public void registerSpawnRule(Class<? extends PowerUpSpawnRule<?>> spawnRuleType) {
        for (var at : spawnRuleType.getAnnotations()) {
            if(at instanceof SpawnRuleType spawnRuleTypeAnnotation) {
                registerSpawnRule(spawnRuleTypeAnnotation.getName(), spawnRuleType);
                return;
            }
        }

        throw new IllegalArgumentException(spawnRuleType + " does not have a defined @" + PowerUpType.class);
    }

    @Override
    public PowerUp createPowerUp(String name, ZombiesArena arena) {
        ensureLoad();
        Validate.isTrue(dataMap.containsKey(name), name + " power up does not exist");
        var data = dataMap.get(name);
        return typeMap.get(data.getPowerUpType()).getLeft().apply(data, arena);
    }

    @Override
    public PowerUpSpawnRule<?> createSpawnRule(String name, String powerUpName, ZombiesArena arena) {
        ensureLoad();
        Validate.isTrue(spawnRuleDataMap.containsKey(name), "spawn rule " + name + " does not exist");
        var data = spawnRuleDataMap.get(name);
        return spawnRuleTypeMap.get(data.getSpawnRuleType()).getLeft().construct(powerUpName, data, arena);
    }

    @Override
    public void load() {
        if(!isLoaded()) {
            isLoading = true;
            addDataLoader(PowerUpDataType.EARNED_GOLD_MOD);
            addDataLoader(PowerUpDataType.MULTIPLIER);
            addDataLoader(PowerUpDataType.MULTIPLIER_WITH_MODE);
            addDataLoader(PowerUpDataType.DAMAGE_MOD);
            addDataLoader(PowerUpDataType.BARRICADE_COUNT_MOD);
            addDataLoader(PowerUpDataType.DURATION);
            addSpawnRuleDataLoader(SpawnRuleDataType.DEFAULT);

            // There is no way to retrieve class from package easily so I gonna manually do it
            registerPowerUp(AmmoModificationPowerUp.class);
            registerPowerUp(DurationPowerUp.class);
            registerPowerUp(EarnedGoldMultiplierPowerUp.class);
            registerPowerUp(PlayerGoldModificationPowerUp.class);
            registerPowerUp(BarricadeCountModificationPowerUp.class);
            registerPowerUp(DamageModificationPowerUp.class);

            registerSpawnRule(DefaultPowerUpSpawnRule.class);

            File[] powerUpFiles = dataLoader.getRootDirectory().listFiles();
            if(powerUpFiles != null) {
                Arrays.stream(powerUpFiles)
                        .map(x -> {try {return dataLoader.load(FilenameUtils.getBaseName(x.getName()), PowerUpData.class); } catch (Exception e) {return null;}})
                        .filter(Objects::nonNull)
                        .forEach(x -> addPowerUpData(x, false));

                Arrays.stream(powerUpFiles)
                        .map(x -> {try {return dataLoader.load(FilenameUtils.getBaseName(x.getName()), SpawnRuleData.class); } catch (Exception e) {return null;}})
                        .filter(Objects::nonNull)
                        .forEach(x -> addSpawnRuleData(x, false));
            }

            loaded = true;
        }
    }

    private void ensureLoad() {
        if(!isLoaded() && !isLoading) {
            load();
        }
    }
}
