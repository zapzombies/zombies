package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.commons.vectors.Vector3D;
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

@MythicAIGoal(name = "breakWindow")
public class BreakWindowGoal extends ZombiesPathfinderGoal<Vector3D> {
    private final double speed;
    private final int breakTicks;
    private final int breakCount;
    private final double breakReachSquared;

    private boolean completed = false;
    private int breakCounter = 0;

    public BreakWindowGoal(@NotNull AbstractEntity entity, @NotNull String line, @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
        this.speed = mlc.getDouble("speed", 1.0D);
        this.breakTicks = mlc.getInteger("breakTicks", 20);
        this.breakCount = mlc.getInteger("breakCount", 1);
        this.breakReachSquared = mlc.getDouble("breakReachSquared", 9D);
    }

    private void pathToWindow() {
        pathHandler.queueOperation(new PathOperationBuilder()
                .withAgent(mob)
                .withDestination(Vectors.asIntFloor(getTarget()))
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
    protected boolean canStart() {
        return !completed;
    }

    @Override
    protected boolean canStop() {
        return Vectors.distanceSquared(Vectors.of(mob.getLocation()), getTarget()) <= 2;
    }

    @Override
    protected void stop() {
        ZombiesArena arena = getArena();
        WindowData window = getSpawnWindow();

        Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);
        if(attackingEntity != null && mob.getUniqueId() == attackingEntity.getUniqueId()) {
            window.getAttackingEntityProperty().setValue(arena, null);
        }

        completed = true;
    }

    @Override
    public void start() {
        pathToWindow();
    }

    @Override
    public void tick() {
        PathResult result = pathHandler.tryTakeResult();

        if(result != null) {
            mobNavigator.navigateAlongPath(result.toPathEntity(), speed);
        }

        if(++breakCounter >= breakTicks) {
            WindowData window = getSpawnWindow();
            if(mob.getEyeLocation().toVector().distanceSquared(window.getCenter()) < breakReachSquared) {
                getArena().tryBreakWindow(mob, window, breakCount);
            }

            breakCounter = 0;
        }

        PathEntityWrapper currentPath = mobNavigator.currentPath();
        if(currentPath == null || currentPath.hasFinished()) {
            pathToWindow();
        }
    }
}