package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.arenaapi.pathfind.calculate.SuccessConditions;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class DeviatingGoal extends PlayerTargetingGoal {
    protected final double targetDeviationSquared;
    protected final boolean requiresSight;

    public DeviatingGoal(@NotNull Plugin plugin, @NotNull AbstractEntity entity, @NotNull String line,
                         @NotNull MythicLineConfig mlc) {
        super(plugin, entity, line, mlc);
        this.targetDeviationSquared = mlc.getDouble("targetDeviationSquared", 0);
        this.requiresSight = mlc.getBoolean("requiresSight", false);
    }


    @Override
    protected @NotNull PathOperation makeOperation(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Player target) {
        return new PathOperationBuilder()
                .withAgent(mob)
                .withDestination(target, zombiesPlayer)
                .withRange(getArena().getMapBounds())
                .withSuccessCondition(SuccessConditions.whenWithin(targetDeviationSquared > 1 ? (requiresSight ?
                        (mob.hasLineOfSight(target) ? targetDeviationSquared : 0) : targetDeviationSquared) :
                        targetDeviationSquared))
                .build();
    }
}
