package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.calculate.SuccessCondition;
import io.github.zap.arenaapi.pathfind.calculate.SuccessConditions;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.destination.PathDestinations;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@MythicAIGoal(name = "zombiesMeleeAttack")
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
    protected @NotNull PathOperation makeOperation(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Player target) {
        return new PathOperationBuilder(mob, PathDestinations.fromEntity(target, arenaNMS.worldBridge(), zombiesPlayer,
                true))
                .withRange(getArena().getMapBounds())
                .withSuccessCondition(SuccessConditions.whenWithin(2))
                .build();
    }

    @Override
    protected void begin() {
        super.begin();
        this.attackTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();

        Player targetPlayer = getCurrentTarget().getPlayer();
        if(targetPlayer != null) {
            attackTimer--;
            tryAttack(targetPlayer);
        }
    }

    private void tryAttack(LivingEntity target) {
        if(this.attackTimer <= 0 && canHit(target)) {
            this.attackTimer = attackInterval;
            mob.swingMainHand();
            mob.attack(target);
        }
    }

    private boolean canHit(LivingEntity target) {
        return target.getLocation().distanceSquared(mob.getLocation()) <= (mob.getWidth() * mob.getWidth() *
                attackReachSquared + target.getWidth());
    }
}
