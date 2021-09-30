package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.commons.vectors.Vector3I;
import io.github.zap.commons.vectors.Vectors;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerTargetingGoal extends ZombiesPathfinderGoal<ZombiesPlayer> {
    private static final int RECALCULATE_INTERVAL = 10;
    private static final int HALF_INTERVAL = RECALCULATE_INTERVAL / 2;

    private final int retargetInterval;

    private int retargetCounter = 0;
    private int recalculateCounter = 0;
    private Vector3I lastLocation = null;

    public PlayerTargetingGoal(@NotNull Plugin plugin, @NotNull AbstractEntity entity, @NotNull String line,
                               @NotNull MythicLineConfig mlc) {
        super(plugin, entity, line, mlc);
        this.retargetInterval = mlc.getInteger("retargetInterval", 20);
    }

    private void calculatePath(ZombiesPlayer player) {
        Player bukkitPlayer = player.getPlayer();

        if(bukkitPlayer != null) {
            pathHandler.giveOperation(makeOperation(player, bukkitPlayer), mob.getWorld());
            lastLocation = Vectors.asIntFloor(Vectors.of(bukkitPlayer.getLocation()));
        }
    }

    private boolean validGamemode(Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode == GameMode.ADVENTURE || gameMode == GameMode.SURVIVAL;
    }

    private boolean locationChanged() {
        ZombiesPlayer target = getCurrentTarget();
        Player bukkitPlayer = target.getPlayer();
        if(bukkitPlayer != null && lastLocation != null) {
            return !Vectors.equals(lastLocation, Vectors.asIntFloor(Vectors.of(bukkitPlayer.getLocation())));
        }

        return true;
    }

    private boolean canStartInternal() {
        ZombiesPlayer target = getCurrentTarget();
        Player bukkitPlayer = target.getPlayer();
        if(bukkitPlayer != null) {
            return target.isInGame() && target.isAlive() && validGamemode(bukkitPlayer);
        }

        return false;
    }

    protected abstract @NotNull PathOperation makeOperation(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Player target);

    @Override
    public @Nullable ZombiesPlayer acquireTarget() {
        double closestDistance = Double.MAX_VALUE;
        ZombiesPlayer closest = null;
        for(ZombiesPlayer player : getArena().getPlayerMap().values()) {
            Player bukkitPlayer = player.getPlayer();
            if(player.isAlive() && player.isInGame() && bukkitPlayer != null && validGamemode(bukkitPlayer)) {
                Location mobLocation = mob.getLocation();
                Location playerLocation = bukkitPlayer.getLocation();

                if(mobLocation.getWorld() == playerLocation.getWorld()) {
                    double distance = mobLocation.distanceSquared(playerLocation);

                    if(distance < closestDistance) {
                        closestDistance = distance;
                        closest = player;
                    }
                }
            }
        }

        return closest;
    }

    @Override
    protected boolean canBegin() {
        return canStartInternal();
    }

    @Override
    protected boolean canStop() {
        return !canStartInternal();
    }

    @Override
    protected void begin() {
        recalculateCounter = 0;
        retargetCounter = 0;
        lastLocation = null;
        zombiesNMS.entityBridge().setAggressive(mob, true);
        mob.setTarget(getCurrentTarget().getPlayer());
        calculatePath(getCurrentTarget());
    }

    @Override
    protected void stop() {
        zombiesNMS.entityBridge().setAggressive(mob, false);
        mob.setTarget(null);
    }

    @Override
    protected void onRetarget(@Nullable ZombiesPlayer newTarget) {
        super.onRetarget(newTarget);

        Player player;
        if(newTarget != null && (player = newTarget.getPlayer()) != null) {
            mob.setTarget(player);
            lastLocation = null;
            recalculateCounter = 0;
        }

        mob.setTarget(null);
    }

    @Override
    public void tick() {
        PathResult result = pathHandler.tryTakeResult();
        MobNavigator navigator = getNavigator();

        if(result != null) {
            navigator.navigateAlongPath(result.toPathEntity(), 1);
        }

        PathEntityWrapper currentPath = navigator.currentPath();
        ZombiesPlayer currentTarget = getCurrentTarget();
        Player bukkitPlayer = currentTarget.getPlayer();

        if(bukkitPlayer != null) {
            mob.lookAt(bukkitPlayer);
        }

        if(++retargetCounter >= retargetInterval) {
            retarget();
            retargetCounter = 0;
        }
        else if(currentPath == null || navigator.isIdle() ||
                (locationChanged() && ++recalculateCounter >= RECALCULATE_INTERVAL)) {
            calculatePath(getCurrentTarget());

            recalculateCounter = (int)(Math.random() * HALF_INTERVAL) -
                    (currentPath == null ? 0 : currentPath.pathLength());
        }
    }
}
