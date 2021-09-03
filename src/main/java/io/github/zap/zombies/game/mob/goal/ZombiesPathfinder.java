package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.ArenaNMSBridge;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.pathfind.engine.PathfinderEngine;
import io.github.zap.arenaapi.pathfind.engine.PathfinderEngines;
import io.github.zap.arenaapi.pathfind.util.PathHandler;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.nms.common.ZombiesNMSBridge;
import lombok.Getter;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * General pathfinding class for Zombies. Supports lazy loading of entity metadata from MythicMobs; subclass pathfinding
 * functions will not be called until all required metadata has been loaded.
 */
public abstract class ZombiesPathfinder {
    private static final PathfinderEngine asyncPathfinder = PathfinderEngines.proxyAsync(Zombies.getInstance());

    protected final Mob self;

    @Getter
    private final String[] metadataKeys;

    @Getter
    private final ArenaNMSBridge arenaNmsBridge;

    @Getter
    private final ZombiesNMSBridge zombiesNmsBridge;

    @Getter
    private final MobNavigator navigator;

    @Getter
    private final PathHandler handler;

    protected final int retargetTicks;

    private final Map<String, Object> metadata = new HashMap<>();
    private boolean metadataLoaded;

    public ZombiesPathfinder(Mob mob, AttributeValue[] values, int retargetTicks, String... metadataKeys) {
        this.self = mob;
        this.metadataKeys = metadataKeys;
        this.metadataLoaded = metadataKeys.length == 0;
        arenaNmsBridge = ArenaApi.getInstance().getNmsBridge();
        zombiesNmsBridge = Zombies.getInstance().getNmsBridge();

        handler = new PathHandler(asyncPathfinder);

        try {
            navigator = ArenaApi.getInstance().getNmsBridge().entityBridge().overrideNavigatorFor(mob);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ArenaApi.info(e.getMessage());
            throw new UnsupportedOperationException("Failed to reflect entity navigator!");
        }

        if (!Zombies.getInstance().getNmsBridge().entityBridge().replacePersistentGoals(self)) {
            Zombies.warning("Failed to replace persistent goals on a " + self.getClass().getName() + " due to a " +
                    "reflection-related exception.");
        }

        for(AttributeValue value : values) {
            arenaNmsBridge.entityBridge().setAttributeFor(self, value.attribute(), value.value());
        }

        this.retargetTicks = retargetTicks;
    }

    /**
     * Gets the metadata value for the given string. Will throw ClassCastException if the metadata type does not match.
     * Will return null if the metadata itself is null, or if there is no metadata associated with the given value.
     * @param key The name of the metadata to get
     * @param <T> The type of the metadata
     * @return The metadata, after casting to T
     */
    public <T> T getMetadata(String key) {
        //noinspection unchecked
        return (T)metadata.get(key);
    }

    /**
     * Gets the metadata value for the given string. Will throw ClassCastException if the metadata type does not match.
     * Accepts a generic Class, to whose type the metadata will be cast.
     * @param key The name of the metadata to get
     * @param dummy The Class which supplies the generic type parameter
     * @param <T> The type of the metadata
     * @return The metadata, after casting to T
     */
    public <T> T getMetadata(String key, Class<T> dummy) {
        //noinspection unchecked
        return (T)metadata.get(key);
    }

    public boolean isValid() {
        return true;
    }

    public final boolean shouldStart() {
        if(!metadataLoaded) {
            for(String key : metadataKeys) {
                MetadataValue value = MetadataHelper.getMetadataFor(self, Zombies.getInstance(), key);

                if(value != null) {
                    this.metadata.put(key, value.value());
                }
                else {
                    return false;
                }
            }

            metadataLoaded = true;
        }

        return canStart();
    }

    public abstract boolean canStart();

    public abstract boolean shouldEnd();

    public abstract void start();

    public abstract void tick();

    public abstract void end();

}
