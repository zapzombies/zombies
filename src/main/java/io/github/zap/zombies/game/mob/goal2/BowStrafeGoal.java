package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.zombies.Zombies;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.jetbrains.annotations.NotNull;

@MythicAIGoal(name = "unboundedArrowAttackWithStrafe")
public class BowStrafeGoal extends PlayerTargetingGoal {
    public BowStrafeGoal(@NotNull AbstractEntity entity, @NotNull String line,
                         @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
    }

    @Override
    public void tick() {
        super.tick();


    }
}
