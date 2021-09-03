package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@MythicMechanic(
        name = "stealCoins",
        description = "Steals coins from the target ZombiesPlayer."
)
public class StealCoinsMechanic extends ZombiesPlayerSkill {
    private static final Random RNG = new Random();

    private final int stealMax;
    private final int stealMin;

    public StealCoinsMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        stealMin = mlc.getInteger("stealMin",10);
        stealMax = mlc.getInteger("stealMax", 100);
    }

    @Override
    public boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena, @NotNull ZombiesPlayer target) {
        target.setCoins(target.getCoins() - (stealMin + RNG.nextInt(stealMax - stealMin)));
        return true;
    }
}
