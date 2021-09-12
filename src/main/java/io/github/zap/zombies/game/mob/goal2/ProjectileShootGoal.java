package io.github.zap.zombies.game.mob.goal2;

import com.destroystokyo.paper.entity.RangedEntity;
import io.github.zap.arenaapi.pathfind.calculate.SuccessConditions;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.util.MathUtils;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProjectileShootGoal extends PlayerTargetingGoal {
    private final RangedEntity rangedMob;

    private final int minIntervalTicks;
    private final int maxIntervalTicks;
    private final double maxShootRange;
    private final double targetDeviation;

    private int updateCountdownCounter;

    public ProjectileShootGoal(@NotNull AbstractEntity entity, @NotNull String line, @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
        this.rangedMob = (RangedEntity) mob;

        this.minIntervalTicks = mlc.getInteger("minIntervalTicks");
        this.maxIntervalTicks = mlc.getInteger("maxIntervalTicks");
        this.maxShootRange = mlc.getDouble("shootRange", 15D);
        this.targetDeviation = mlc.getDouble("targetDeviation", 5);
    }

    @Override
    protected @NotNull PathOperation makeOperation(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Player target) {
        return new PathOperationBuilder()
                .withAgent(mob)
                .withDestination(target, zombiesPlayer)
                .withRange(getArena().getMapBounds())
                .withSuccessCondition(SuccessConditions.whenWithin(rangedMob.hasLineOfSight(target) ? targetDeviation : 0))
                .build();
    }

    @Override
    protected void stop() {
        super.stop();

        updateCountdownCounter = -1;
    }

    @Override
    public void tick() {
        super.tick();

        ZombiesPlayer target = getTarget();
        Player bukkitPlayer;
        if(target != null && (bukkitPlayer = target.getPlayer()) != null) {
            double distanceSquared = rangedMob.getLocation().distanceSquared(bukkitPlayer.getLocation());
            boolean canSee = rangedMob.hasLineOfSight(bukkitPlayer);

            rangedMob.lookAt(bukkitPlayer);

            double f;
            if(--updateCountdownCounter == 0) {
                if(!canSee) {
                    return;
                }

                f = Math.sqrt(distanceSquared) / maxShootRange;
                double g = MathUtils.clamp(f, 0.1D, 1.0D);
                rangedMob.rangedAttack(bukkitPlayer, (float)g);
                updateCountdownCounter = (int)Math.floor(f * (maxIntervalTicks - minIntervalTicks) + minIntervalTicks);
            }
            else if(updateCountdownCounter < 0) {
                f = Math.sqrt(distanceSquared) / maxShootRange;
                updateCountdownCounter = (int)Math.floor(f * (maxIntervalTicks - minIntervalTicks) + minIntervalTicks);
            }
        }
    }
}
