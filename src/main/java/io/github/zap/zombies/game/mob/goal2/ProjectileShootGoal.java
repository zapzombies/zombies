package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.util.MathUtils;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@MythicAIGoal(name = "zombiesProjectileShoot")
public class ProjectileShootGoal extends RangedGoal {
    private final double shootRange;

    private int updateCountdownCounter;

    public ProjectileShootGoal(@NotNull AbstractEntity entity, @NotNull String line, @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
        shootRange = Math.sqrt(super.shootRangeSquared);
    }

    @Override
    protected void stop() {
        super.stop();
        updateCountdownCounter = -1;
    }

    @Override
    public void tick() {
        super.tick();

        Player targetPlayer = getCurrentTarget().getPlayer();
        if(targetPlayer != null) {
            double distanceSquared = rangedEntity.getLocation().distanceSquared(targetPlayer.getLocation());
            rangedEntity.lookAt(targetPlayer);

            if(--updateCountdownCounter == 0) {
                if(!rangedEntity.hasLineOfSight(targetPlayer)) {
                    return;
                }

                double f = Math.sqrt(distanceSquared) / shootRange;
                double g = MathUtils.clamp(f, 0.1D, 1.0D);

                rangedEntity.rangedAttack(targetPlayer, (float)g);
                updateCountdownCounter = (int)Math.floor(f * 5 + shootInterval);
            }
            else if(updateCountdownCounter < 0) {
                updateCountdownCounter = (int)Math.floor((Math.sqrt(distanceSquared) / shootRange) * 5 + shootInterval);
            }
        }
    }
}
