package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.util.MathUtils;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProjectileShootGoal extends RangedGoal {
    private final int minIntervalTicks;
    private final int maxIntervalTicks;
    private final double maxShootRange;

    private int updateCountdownCounter;

    public ProjectileShootGoal(@NotNull AbstractEntity entity, @NotNull String line, @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
        this.minIntervalTicks = mlc.getInteger("minIntervalTicks");
        this.maxIntervalTicks = mlc.getInteger("maxIntervalTicks");
        this.maxShootRange = mlc.getDouble("shootRange", 15D);
    }

    @Override
    protected void stop() {
        super.stop();

        updateCountdownCounter = -1;
    }

    @Override
    public void tick() {
        super.tick();

        Player targetPlayer = getTarget().getPlayer();
        if(targetPlayer != null) {
            double distanceSquared = rangedEntity.getLocation().distanceSquared(targetPlayer.getLocation());
            boolean canSee = rangedEntity.hasLineOfSight(targetPlayer);

            rangedEntity.lookAt(targetPlayer);

            double f;
            if(--updateCountdownCounter == 0) {
                if(!canSee) {
                    return;
                }

                f = Math.sqrt(distanceSquared) / maxShootRange;
                double g = MathUtils.clamp(f, 0.1D, 1.0D);
                rangedEntity.rangedAttack(targetPlayer, (float)g);
                updateCountdownCounter = (int)Math.floor(f * (maxIntervalTicks - minIntervalTicks) + minIntervalTicks);
            }
            else if(updateCountdownCounter < 0) {
                f = Math.sqrt(distanceSquared) / maxShootRange;
                updateCountdownCounter = (int)Math.floor(f * (maxIntervalTicks - minIntervalTicks) + minIntervalTicks);
            }
        }
    }
}
