package io.github.zap.zombies.game.mob.goal;

import com.destroystokyo.paper.entity.RangedEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class OptimizedBowAttack extends RetargetingPathfinder {
    private final int attackInterval;
    private final float shootDistanceSquared;
    private int attackTimer = -1;
    private int drawTimer;
    private boolean strafeB;
    private boolean strafeA;
    private int strafeTimer = -1;

    public OptimizedBowAttack(Mob mob, AttributeValue[] values, int retargetTicks, double speed,
                              double targetDeviation, int attackInterval, float shootDistanceSquared) {
        super(mob, values, retargetTicks, speed, targetDeviation);
        this.attackInterval = attackInterval;
        this.shootDistanceSquared = shootDistanceSquared;
    }

    public boolean isValid() {
        return getZombiesNmsBridge().entityBridge().isAbstractSkeleton(self);
    }

    @Override
    public void start() {
        getZombiesNmsBridge().entityBridge().setAggressive(self, true);
    }

    @Override
    public void end() {
        this.drawTimer = 0;
        this.attackTimer = -1;
        self.clearActiveItem();
        getZombiesNmsBridge().entityBridge().setAggressive(self, false);
        self.setTarget(null);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = self.getTarget();

        if (target != null) {
            Location location = target.getLocation();
            double distanceToTargetSquared = getArenaNmsBridge().entityBridge().distanceTo(self, location.getX(),
                    location.getY(), location.getZ());
            boolean hasSight = getArenaNmsBridge().entityBridge().canSee(self, target);

            boolean bowPartiallyDrawn = this.drawTimer > 0;
            if (hasSight != bowPartiallyDrawn) {
                this.drawTimer = 0;
            }

            if (hasSight) {
                ++this.drawTimer;
            } else {
                --this.drawTimer;
            }

            if (!(distanceToTargetSquared > (double)this.shootDistanceSquared) && this.drawTimer >= 20) {
                ++this.strafeTimer;
            } else {
                this.strafeTimer = -1;
            }

            if (this.strafeTimer >= 20) {
                if ((double) getArenaNmsBridge().entityBridge().getRandomFor(self).nextFloat() < 0.3D) {
                    this.strafeB = !this.strafeB;
                }

                if ((double) getArenaNmsBridge().entityBridge().getRandomFor(self).nextFloat() < 0.3D) {
                    this.strafeA = !this.strafeA;
                }

                this.strafeTimer = 0;
            }

            if (this.strafeTimer > -1) {
                if (distanceToTargetSquared > (double)(this.shootDistanceSquared * 0.75F)) {
                    this.strafeA = false;
                } else if (distanceToTargetSquared < (double)(this.shootDistanceSquared * 0.25F)) {
                    this.strafeA = true;
                }

                getZombiesNmsBridge().entityBridge().strafe(self, this.strafeA ? -0.5F : 0.5F, this.strafeB ? 0.5F : -0.5F);
                getArenaNmsBridge().entityBridge().setLookDirection(self, target, 30.0F, 30.0F);
            } else {
                self.lookAt(target, 30.0F, 30.0F);
            }

            if (self.isHandRaised()) {
                if (!hasSight && this.drawTimer < -60) {
                    self.clearActiveItem();
                } else if (hasSight && distanceToTargetSquared < shootDistanceSquared) {
                    int itemStage = getZombiesNmsBridge().entityBridge().getTicksUsingItem(self);
                    if (itemStage >= 20) {
                        self.clearActiveItem();
                        ((RangedEntity) self).rangedAttack(target, getZombiesNmsBridge().entityBridge().getCharge(itemStage));
                        this.attackTimer = this.attackInterval;
                    }
                }
            } else if (--this.attackTimer <= 0 && this.drawTimer >= -60
                    && distanceToTargetSquared < shootDistanceSquared) {
                getZombiesNmsBridge().entityBridge().startPullingBow(self);
            }
        }
    }
}
