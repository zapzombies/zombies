package io.github.zap.zombies.game.mob.goal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public abstract class RetargetingPathfinder extends BasicMetadataPathfinder {
    private int retargetCounter;

    public RetargetingPathfinder(Mob mob, AttributeValue[] values, int retargetTicks, double speed,
                                 double targetDeviation) {
        super(mob, values, retargetTicks, speed, targetDeviation);
        retargetCounter = getArenaNmsBridge().entityBridge().getRandomFor(self).nextInt(retargetTicks);
    }

    @Override
    public void tick() {
        LivingEntity target = self.getTarget();

        if(target != null) {
            self.lookAt(target, 30.0F, 30.0F);
        }

        if (++retargetCounter == retargetTicks) {
            //randomly offset the navigation so we don't flood the pathfinder
            this.retargetCounter = getArenaNmsBridge().entityBridge().getRandomFor(self).nextInt(retargetTicks / 2);
            retarget();
        }

        if(result != null) {
            setPath(result);
            result = null;
        }
    }

    @Override
    public boolean canStart() {
        LivingEntity target = self.getTarget();

        if (target != null) {
            return target.getWorld().getUID().equals(self.getWorld().getUID());
        }

        return true;
    }

    @Override
    public boolean shouldEnd() {
        return !canStart();
    }
}
