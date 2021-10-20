package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.destination.PathDestinations;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.process.PostProcessors;
import io.github.zap.commons.vectors.Vector3D;
import io.github.zap.commons.vectors.Vector3I;
import io.github.zap.commons.vectors.Vectors;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MythicAIGoal(name = "zombiesBreakWindow")
public class BreakWindowGoal extends ZombiesPathfinderGoal<Vector3D> {
    private static final int RECALCULATE_INTERVAL = 20;

    private final WorldBridge worldBridge;

    private final int breakInterval;
    private final int breakCount;
    private final double breakReachSquared;

    private boolean completed = false;
    private int breakCounter = 0;
    private int recalculateCounter = 0;

    public BreakWindowGoal(@NotNull AbstractEntity entity, @NotNull String line, @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
        this.worldBridge = ArenaApi.getInstance().getNmsBridge().worldBridge();
        this.breakInterval = mlc.getInteger("breakInterval", 20);
        this.breakCount = mlc.getInteger("breakCount", 1);
        this.breakReachSquared = mlc.getDouble("breakReachSquared", 9D);
    }

    private void pathToWindow() {
        Vector3I target = Vectors.asIntFloor(getCurrentTarget());

        pathHandler.giveOperation(new PathOperationBuilder(mob, PathDestinations.basic(target))
                .withRange(3)
                .build(), mob.getWorld());
    }

    @Override
    public @Nullable Vector3D acquireTarget() {
        WindowData windowData = getSpawnWindow();

        if(windowData != null) {
            return Vectors.of(windowData.getTarget());
        }

        return null;
    }

    @Override
    protected boolean canBegin() {
        return !completed;
    }

    @Override
    protected boolean canStop() {
        Vector3D target = getCurrentTarget();
        return Vectors.distanceSquared(Vectors.of(mob.getLocation()), target) <= 1.5;
    }

    @Override
    protected void stop() {
        ZombiesArena arena = getArena();
        WindowData window = getSpawnWindow();

        Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);
        if(attackingEntity != null && mob.getUniqueId().equals(attackingEntity.getUniqueId())) {
            window.getAttackingEntityProperty().setValue(arena, null);
        }

        completed = true;
    }

    @Override
    protected void begin() {
        pathToWindow();
    }

    @Override
    public void tick() {
        PathResult result = pathHandler.tryTakeResult();
        MobNavigator navigator = getNavigator();

        if(result != null) {
            navigator.navigateAlongPath(result.toPathEntity(), 1);
        }

        if(++breakCounter >= breakInterval) {
            WindowData window = getSpawnWindow();
            if(mob.getEyeLocation().toVector().distanceSquared(window.getCenter()) < breakReachSquared) {
                getArena().tryBreakWindow(mob, window, breakCount);
            }

            breakCounter = 0;
        }

        PathEntityWrapper currentPath = navigator.currentPath();
        if(currentPath == null || (!navigator.hasActivePath() && ++recalculateCounter >= RECALCULATE_INTERVAL)) {
            pathToWindow();
            recalculateCounter = 0;
        }
    }
}