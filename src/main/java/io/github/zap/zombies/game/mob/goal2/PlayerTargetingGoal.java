package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.commons.vectors.Vector3I;
import io.github.zap.commons.vectors.Vectors;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class PlayerTargetingGoal extends ZombiesPathfinderGoal<ZombiesPlayer> {
    private final double speed;
    private final int retargetTicks;

    private int retargetCounter = 0;
    private Vector3I lastLocation = null;

    public PlayerTargetingGoal(@NotNull Plugin plugin, @NotNull AbstractEntity entity, @NotNull String line,
                               @NotNull MythicLineConfig mlc) {
        super(plugin, entity, line, mlc);
        this.speed = mlc.getDouble("speed", 1D);
        this.retargetTicks = mlc.getInteger("retargetTicks", 20);
    }

    private void pathToPlayer(ZombiesPlayer player) {
        Player bukkitPlayer = player.getPlayer();

        if(bukkitPlayer != null) {
            PathOperation operation = new PathOperationBuilder()
                    .withAgent(mob)
                    .withDestination(bukkitPlayer, player)
                    .withRange(getArena().getMapBounds())
                    .build();

            pathHandler.queueOperation(operation, mob.getWorld());
            lastLocation = Vectors.asIntFloor(Vectors.of(bukkitPlayer.getLocation()));
        }
    }

    private boolean locationChanged() {
        return Vectors.equals(lastLocation, Vectors.asIntFloor(Vectors.of(mob.getLocation())));
    }

    @Override
    public @Nullable ZombiesPlayer acquireTarget() {
        double closestDistance = Double.MAX_VALUE;
        ZombiesPlayer closest = null;
        for(ZombiesPlayer player : getArena().getPlayerMap().values()) {
            Player bukkitPlayer = player.getPlayer();
            if(player.isAlive() && player.isInGame() && bukkitPlayer != null) {
                double distance = mob.getLocation().distance(bukkitPlayer.getLocation());

                if(distance < closestDistance) {
                    closestDistance = distance;
                    closest = player;
                }
            }
        }

        return closest;
    }

    @Override
    protected boolean canStart() {
        ZombiesPlayer target = getTarget();
        Player bukkitPlayer = target.getPlayer();
        if(bukkitPlayer != null) {
            GameMode gameMode = bukkitPlayer.getGameMode();
            return target.isInGame() && target.isAlive() &&
                    (gameMode == GameMode.ADVENTURE || gameMode == GameMode.SURVIVAL);
        }

        return false;
    }

    @Override
    protected boolean canStop() {
        ZombiesPlayer target = getTarget();
        Player bukkitPlayer = target.getPlayer();
        if(bukkitPlayer != null) {
            GameMode gameMode = bukkitPlayer.getGameMode();
            return !target.isInGame() || !target.isAlive() ||
                    gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
        }

        return true;
    }

    @Override
    public void start() {
        zombiesNMS.entityBridge().setAggressive(mob, true);
        mob.setTarget(getTarget().getPlayer());
    }

    @Override
    protected void stop() {
        zombiesNMS.entityBridge().setAggressive(mob, false);
        mob.setTarget(null);
    }

    @Override
    public void tick() {
        PathEntityWrapper currentPath = mobNavigator.currentPath();

        if(++retargetCounter >= retargetTicks) {
            reset();
            retargetCounter = 0;
        }
        else if(currentPath == null || currentPath.hasFinished() || locationChanged()) {
            pathToPlayer(getTarget());
        }

        PathResult result = pathHandler.tryTakeResult();
        if(result != null) {
            mobNavigator.navigateAlongPath(result.toPathEntity(), speed);
        }
    }
}
