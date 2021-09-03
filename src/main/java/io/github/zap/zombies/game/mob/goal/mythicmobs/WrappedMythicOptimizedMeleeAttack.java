package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.zombies.game.mob.goal.AttributeValue;
import io.github.zap.zombies.game.mob.goal.OptimizedMeleeAttack;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;

@MythicAIGoal(name = "unboundedMeleeAttack")
public class WrappedMythicOptimizedMeleeAttack extends Pathfinder {

    private final OptimizedMeleeAttack optimizedMeleeAttack;

    public WrappedMythicOptimizedMeleeAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);

        AttributeValue[] attributes;
        if (entity.getBukkitEntity() instanceof Mob mob) {
            ActiveMob activeMob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(mob);
            if (activeMob != null) {
                if (!ArenaApi.getInstance().getNmsBridge().entityBridge()
                        .hasAttribute(mob, Attribute.GENERIC_ATTACK_DAMAGE)) {
                    attributes = new AttributeValue[] {
                            new AttributeValue(Attribute.GENERIC_ATTACK_DAMAGE,
                                    activeMob.getDamage() == 0 ? 2F : activeMob.getDamage()),
                            new AttributeValue(Attribute.GENERIC_ATTACK_KNOCKBACK,
                                    mlc.getDouble("knockback", 0))
                    };
                }
                else {
                    attributes = new AttributeValue[0];
                }
            }
            else {
                attributes = new AttributeValue[0];
            }

            this.optimizedMeleeAttack = new OptimizedMeleeAttack(mob, attributes,
                    mlc.getInteger("retargetTicks", 10), mlc.getDouble("speed", 1),
                    mlc.getDouble("targetDeviation", 0.5), mlc.getInteger("attackTicks", 20),
                    mlc.getDouble("attackReachSquared", 2));
        }
        else throw new IllegalArgumentException("Tried to apply pathfinder to mob with UUID "
                + entity.getBukkitEntity().getUniqueId() + " of invalid class "
                + entity.getBukkitEntity().getClass().getName() + "!");
    }

    @Override
    public boolean isValid() {
        return optimizedMeleeAttack.isValid();
    }

    @Override
    public boolean shouldStart() {
        return optimizedMeleeAttack.shouldStart();
    }

    @Override
    public void start() {
        optimizedMeleeAttack.start();
    }

    @Override
    public void tick() {
        optimizedMeleeAttack.tick();
    }

    @Override
    public boolean shouldEnd() {
        return optimizedMeleeAttack.shouldEnd();
    }

    @Override
    public void end() {
        optimizedMeleeAttack.end();
    }

}
