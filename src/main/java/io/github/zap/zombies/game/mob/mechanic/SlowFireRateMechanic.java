package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@MythicMechanic(
        name = "slowFireRate",
        description = "Slows the fire rate of the target player by a configurable amount."
)
public class SlowFireRateMechanic extends ZombiesPlayerSkill {
    private static final String SLOW_FIRE_RATE_MODIFIER_NAME = "zz_slow_low_iq";

    private final double speedModifier;
    private final int duration;

    private int slowdownTaskId = -1;

    public SlowFireRateMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        speedModifier = mlc.getDouble("speedModifier", 0.5);
        duration = mlc.getInteger("duration", 40);
    }

    @Override
    public boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena,
                                @NotNull ZombiesPlayer target) {
        if(target.getPlayer() != null) {
            if(!target.getFireRateMultiplier().hasModifier(SLOW_FIRE_RATE_MODIFIER_NAME)) {
                target.getFireRateMultiplier().registerModifier(SLOW_FIRE_RATE_MODIFIER_NAME, d -> d == null ? 0D :
                        d * speedModifier);

                slowdownTaskId = arena.runTaskLater(duration, () -> target.getFireRateMultiplier()
                        .removeModifier(SLOW_FIRE_RATE_MODIFIER_NAME)).getTaskId();
            }
            else {
                Bukkit.getScheduler().cancelTask(slowdownTaskId);

                slowdownTaskId = arena.runTaskLater(duration, () -> target.getFireRateMultiplier()
                        .removeModifier(SLOW_FIRE_RATE_MODIFIER_NAME)).getTaskId();
            }
        }

        return true;
    }
}
