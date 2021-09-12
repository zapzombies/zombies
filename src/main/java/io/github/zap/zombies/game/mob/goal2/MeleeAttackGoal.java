package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@MythicAIGoal(name = "unboundedMeleeAttack")
public class MeleeAttackGoal extends PlayerTargetingGoal {
    private final int attackInterval;
    private final double attackReachSquared;

    private int attackTimer;

    public MeleeAttackGoal(@NotNull AbstractEntity entity, @NotNull String line, @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
        this.attackInterval = mlc.getInteger("attackInterval", 10);
        this.attackReachSquared = mlc.getDouble("attackReachSquared", 2);
    }

    @Override
    public void start() {
        super.start();
        this.attackTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();

        ZombiesPlayer target = getTarget();

        if(target != null) {
            Player bukkitPlayer = target.getPlayer();

            if(bukkitPlayer != null) {
                attackTimer--;
                tryAttack(bukkitPlayer);
            }
        }
    }

    private void tryAttack(LivingEntity target) {
        if(this.attackTimer <= 0) {
            if(target.getLocation().distanceSquared(mob.getLocation()) <= checkDistance(target)) {
                this.attackTimer = attackInterval;
                mob.swingMainHand();
                mob.attack(target);
            }
        }
    }

    private double checkDistance(LivingEntity target) {
        return (mob.getWidth() * mob.getWidth() * attackReachSquared + target.getWidth());
    }
}
