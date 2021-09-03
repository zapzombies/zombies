package io.github.zap.zombies.game.equipment2.feature.gun;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment2.feature.gun.targeter.TargetSelection;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class HitHandler {

    private final ZombiesArena.DamageHandler damageHandler;

    private final List<Consumer<TargetSelection>> effectCallbacks;

    private final Collection<Shot> childShots;

    private final int goldPerShot;

    private final int goldPerHeadshot;

    private final double shotKnockbackFactor;

    private final double headshotKnockbackFactor;

    private final double damage;

    public HitHandler(@NotNull ZombiesArena.DamageHandler damageHandler,
                      @NotNull List<Consumer<TargetSelection>> effectCallbacks, @NotNull Collection<Shot> childShots,
                      int goldPerShot, int goldPerHeadshot, double shotKnockbackFactor, double headshotKnockbackFactor,
                      double damage) {
        this.damageHandler = damageHandler;
        this.effectCallbacks = effectCallbacks;
        this.childShots = childShots;
        this.goldPerShot = goldPerShot;
        this.goldPerHeadshot = goldPerHeadshot;
        this.shotKnockbackFactor = shotKnockbackFactor;
        this.headshotKnockbackFactor = headshotKnockbackFactor;
        this.damage = damage;
    }

    public void hit(@NotNull MapData map, @NotNull World world, @NotNull Damager damager,
                    @NotNull TargetSelection selection, @NotNull Set<Mob> nextCandidates, @NotNull Set<Mob> used,
                    @NotNull List<Boolean> headshotHistory) {
        damageHandler.damageEntity(damager, new DamageAttempt() {
            @Override
            public int getCoins(@NotNull Damager damager, @NotNull Mob target) {
                return (selection.headshot()) ? goldPerHeadshot : goldPerShot;
            }

            @Override
            public double damageAmount(@NotNull Damager damager, @NotNull Mob target) {
                return damage;
            }

            @Override
            public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
                return selection.headshot();
            }

            @Override
            public @NotNull Vector directionVector(@NotNull Damager damager, @NotNull Mob target) {
                return selection.direction();
            }

            @Override
            public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
                return (selection.headshot()) ? headshotKnockbackFactor : shotKnockbackFactor;
            }
        }, selection.mob());

        for (Consumer<TargetSelection> effectCallback : effectCallbacks) {
            effectCallback.accept(selection);
        }

        headshotHistory.add(selection.headshot());
        for (Shot shot : childShots) {
            shot.shoot(map, world, damager, nextCandidates, used, selection.location(), selection.direction(),
                    new ArrayList<>(headshotHistory));
        }
    }

}
