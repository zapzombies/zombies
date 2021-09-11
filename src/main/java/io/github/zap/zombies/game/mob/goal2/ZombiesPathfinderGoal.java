package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.ArenaNMSBridge;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.pathfind.engine.PathfinderEngine;
import io.github.zap.arenaapi.pathfind.engine.PathfinderEngines;
import io.github.zap.arenaapi.pathfind.util.PathHandler;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.SpawnMetadata;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.nms.common.ZombiesNMSBridge;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.logging.Level;

public abstract class ZombiesPathfinderGoal<T> extends Pathfinder {
    protected static final PathfinderEngine PATHFINDER_ENGINE = PathfinderEngines.proxyAsync(Zombies.getInstance());

    protected final Plugin plugin;

    protected final ArenaNMSBridge arenaNMS;
    protected final ZombiesNMSBridge zombiesNMS;

    protected final Mob mob;
    protected final MobNavigator mobNavigator;
    protected final PathHandler pathHandler;
    protected final boolean successfulLoad;

    private boolean metadataLoaded = false;
    private ZombiesArena arena;
    private WindowData window;

    private T target;

    public ZombiesPathfinderGoal(@NotNull Plugin plugin, @NotNull AbstractEntity entity, @NotNull String line,
                                 @NotNull MythicLineConfig mlc) {
        super(entity, line, mlc);
        this.plugin = plugin;
        arenaNMS = ArenaApi.getInstance().getNmsBridge();
        zombiesNMS = Zombies.getInstance().getNmsBridge();

        Mob mob = null;
        MobNavigator mobNavigator = null;
        PathHandler pathHandler = null;
        boolean successfulLoad = false;

        Entity bukkitEntity = entity.getBukkitEntity();
        if(bukkitEntity instanceof Mob) {
            mob = (Mob)bukkitEntity;

            try {
                mobNavigator = arenaNMS.entityBridge().overrideNavigatorFor(mob);

                try {
                    zombiesNMS.entityBridge().replacePersistentGoals(mob);

                    double damage;
                    if(!Double.isNaN(damage = mlc.getDouble("Damage", Double.NaN))) {
                        setAttributeValue(mob, Attribute.GENERIC_ATTACK_DAMAGE, damage);
                    }

                    pathHandler = new PathHandler(PATHFINDER_ENGINE);
                    successfulLoad = true;
                } catch (NoSuchFieldException | IllegalAccessException exception) {
                    plugin.getLogger().log(Level.SEVERE, "failed to replace persistent goals", exception);
                }
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                plugin.getLogger().log(Level.SEVERE, "failed to create MobNavigator", exception);
            }
        }
        else {
            plugin.getLogger().log(Level.SEVERE, "entity must subclass Mob");
        }

        this.mob = mob;
        this.mobNavigator = mobNavigator;
        this.pathHandler = pathHandler;
        this.successfulLoad = successfulLoad;
    }

    private void setAttributeValue(Mob mob, Attribute attribute, double baseValue) {
        AttributeInstance instance = mob.getAttribute(attribute);

        if(instance == null) {
            mob.registerAttribute(attribute);
            instance = mob.getAttribute(attribute);
        }

        if(instance != null) {
            instance.setBaseValue(baseValue);
        }
        else {
            throw new IllegalArgumentException("unable to set base value of attribute " + attribute);
        }
    }

    private boolean loadMetadata() {
        if(!metadataLoaded) {
            Optional<MetadataValue> optionalSpawnData = MetadataHelper.getMetadataValue(mob, plugin,
                    Zombies.SPAWN_METADATA_NAME);

            if(optionalSpawnData.isPresent()) {
                SpawnMetadata spawnData = (SpawnMetadata)optionalSpawnData.get().value();

                if(spawnData != null) {
                    arena = spawnData.arena();
                    window = spawnData.windowData();
                    metadataLoaded = true;
                }
            }
        }

        return metadataLoaded;
    }

    public abstract @Nullable T acquireTarget();

    public ZombiesArena getArena() {
        return arena;
    }

    public WindowData getSpawnWindow() {
        return window;
    }

    public T getTarget() {
        return target;
    }

    @Override
    public final boolean shouldStart() {
        return successfulLoad && loadMetadata() && target == null && arena.runAI() && (target = acquireTarget()) != null
                && canStart();
    }

    @Override
    public final boolean shouldEnd() {
        return target == null || !arena.runAI() || canStop();
    }

    @Override
    public final void end() {
        target = null;
        stop();
    }

    protected abstract boolean canStart();

    protected abstract boolean canStop();

    protected abstract void stop();
}
