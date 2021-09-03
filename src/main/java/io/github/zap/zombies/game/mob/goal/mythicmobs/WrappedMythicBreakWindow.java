package io.github.zap.zombies.game.mob.goal.mythicmobs;

import io.github.zap.zombies.game.mob.goal.AttributeValue;
import io.github.zap.zombies.game.mob.goal.BreakWindow;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.entity.Mob;

@MythicAIGoal(name = "breakWindow")
public class WrappedMythicBreakWindow extends Pathfinder {

    private final BreakWindow breakWindow;

    public WrappedMythicBreakWindow(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);

        if (entity.getBukkitEntity() instanceof Mob mob) {
            this.breakWindow = new BreakWindow(mob, new AttributeValue[0],
                    mlc.getInteger("retargetTicks", 10), mlc.getDouble("speed", 1),
                    mlc.getDouble("targetDeviation", 0.5), mlc.getInteger("breakTicks", 20),
                    mlc.getInteger("breakCount", 1), mlc.getDouble("breakReachSquared", 9D));
        }
        else throw new IllegalArgumentException("Tried to apply pathfinder to mob with UUID "
                + entity.getBukkitEntity().getUniqueId() + " of invalid class "
                + entity.getBukkitEntity().getClass().getName() + "!");
    }

    @Override
    public boolean isValid() {
        return breakWindow.isValid();
    }

    @Override
    public boolean shouldStart() {
        return breakWindow.shouldStart();
    }

    @Override
    public void start() {
        breakWindow.start();
    }

    @Override
    public void tick() {
        breakWindow.tick();
    }

    @Override
    public boolean shouldEnd() {
        return breakWindow.shouldEnd();
    }

    @Override
    public void end() {
        breakWindow.end();
    }

}
