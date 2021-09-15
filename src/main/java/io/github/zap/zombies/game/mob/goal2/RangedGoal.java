package io.github.zap.zombies.game.mob.goal2;

import com.destroystokyo.paper.entity.RangedEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class RangedGoal extends DeviatingGoal {
    protected final RangedEntity rangedEntity;
    protected final int shootInterval;
    protected final double shootRangeSquared;

    public RangedGoal(@NotNull Plugin plugin, @NotNull AbstractEntity entity, @NotNull String line,
                      @NotNull MythicLineConfig mlc) {
        super(plugin, entity, line, mlc);

        if(mob instanceof RangedEntity rangedEntity) {
            this.rangedEntity = rangedEntity;
            this.shootInterval = mlc.getInteger("shootInterval", 20);
            this.shootRangeSquared = mlc.getDouble("shootRangeSquared", 225D);
        }
        else {
            throw new IllegalArgumentException("mob must subclass RangedEntity");
        }
    }

    @Override
    public void start() {
        super.start();
        rangedEntity.setChargingAttack(true);
    }

    @Override
    protected void stop() {
        super.stop();
        rangedEntity.setChargingAttack(false);
    }
}
