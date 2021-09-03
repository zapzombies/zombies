package io.github.zap.zombies.game.equipment2.feature.gun.headshot;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class BasicHeadshotter implements Headshotter {

    private final boolean inverted;

    public BasicHeadshotter(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public boolean isHeadshot(@NotNull RayTraceResult rayTraceResult, @NotNull List<Boolean> headshotHistory) {
        Entity hit = rayTraceResult.getHitEntity();
        if (hit == null) {
            throw new IllegalArgumentException("Tried to determine a headshot on a raytrace that did not involve " +
                    "a living entity!");
        }
        if (!(hit instanceof LivingEntity livingEntity)) {
            throw new IllegalArgumentException("Tried to determine a headshot on a raytrace that did not involve " +
                    "a living entity!");
        }

        // height - 2 * (height - eyeHeight) = 2 * eyeHeight - height
        double bottomY = 2 * livingEntity.getEyeHeight() - livingEntity.getHeight();
        double hitY = rayTraceResult.getHitPosition().getY();

        boolean headshot = bottomY <= hitY && hitY <= livingEntity.getHeight();

        /*
         * inverted, headshot -> false
         * inverted, !headshot -> true
         * !inverted, headshot -> true
         * !inverted, !headshot -> false
         */
        return headshot != inverted;
    }

}
