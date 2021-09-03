package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.AttributeValue;
import io.github.zap.zombies.game.mob.goal.OptimizedBowAttack;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.entity.Mob;

@MythicAIGoal(name = "unboundedArrowAttackWithStrafe")
public class WrappedMythicOptimizedBowAttack extends Pathfinder {

    private final OptimizedBowAttack optimizedBowAttack;

    public WrappedMythicOptimizedBowAttack(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);

        if (entity.getBukkitEntity() instanceof Mob mob) {
            this.optimizedBowAttack = new OptimizedBowAttack(mob, new AttributeValue[0],
                    mlc.getInteger("retargetTicks", 10), mlc.getDouble("speed", 1),
                    mlc.getDouble("targetDeviation", 0), mlc.getInteger("fireInterval", 20),
                    mlc.getFloat("targetDistance", 225));
        }
        else throw new IllegalArgumentException("Tried to apply pathfinder to mob with UUID "
                + entity.getBukkitEntity().getUniqueId() + " of invalid class "
                + entity.getBukkitEntity().getClass().getName() + "!");

        setGoalType(GoalType.MOVE_LOOK);
    }

    @Override
    public boolean isValid() {
        return optimizedBowAttack.isValid();
    }

    @Override
    public boolean shouldStart() {
        return optimizedBowAttack.shouldStart();
    }

    @Override
    public void start() {
        optimizedBowAttack.start();
    }

    @Override
    public void tick() {
        optimizedBowAttack.tick();
    }

    @Override
    public boolean shouldEnd() {
        return optimizedBowAttack.shouldEnd();
    }

    @Override
    public void end() {
        optimizedBowAttack.end();
    }

}
