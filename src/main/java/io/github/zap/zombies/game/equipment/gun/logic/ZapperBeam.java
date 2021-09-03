package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Beam that "zaps" nearby entities when it hits a target entity
 */
public class ZapperBeam extends LinearBeam {
    private static final Vector ORIGIN = new Vector(0, 0, 0);
    @RequiredArgsConstructor
    private class ZapperAoeDamageAttempt implements DamageAttempt {
        private final Vector kbDirectionVector;

        @Override
        public int getCoins(@NotNull Damager damager, @NotNull Mob target) {
            return getGoldPerShot();
        }

        @Override
        public double damageAmount(@NotNull Damager damager, @NotNull Mob target) {
            return getDamage() * aoeDamageFactor;
        }

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return false;
        }

        @Override
        public @NotNull Vector directionVector(@NotNull Damager damager, @NotNull Mob target) {
            return kbDirectionVector.clone();
        }

        @Override
        public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
            return getKnockbackFactor();
        }
    }

    private final Set<Mob> hitMobs = new HashSet<>();

    private final int maxChainedEntities;

    private final double maxChainDistance;

    private final double aoeDamageFactor;

    public ZapperBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, ZapperGunLevel zapperGunLevel,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper, int particleCount) {
        super(mapData, zombiesPlayer, root, zapperGunLevel, particle, particleDataWrapper, particleCount);

        this.maxChainedEntities = zapperGunLevel.getMaxChainedEntities();
        this.maxChainDistance = zapperGunLevel.getMaxChainDistance();
        this.aoeDamageFactor = zapperGunLevel.getAoeHitDamageFactor();
    }

    public ZapperBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, ZapperGunLevel zapperGunLevel,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper) {
        this(mapData, zombiesPlayer, root, zapperGunLevel, particle, particleDataWrapper, DEFAULT_PARTICLE_COUNT);
    }

    @Override
    protected void hitScan() {
        super.hitScan();

        World world = getWorld();
        Set<Mob> attackedMobs = new HashSet<>(hitMobs);
        for (Mob hitMob : hitMobs) {
            Iterator<Mob> mobsToZap = world.getNearbyEntitiesByType(Mob.class, hitMob.getLocation(), maxChainDistance)
                    .iterator();
            int counter = 0;

            while (mobsToZap.hasNext() && counter < maxChainedEntities) {
                Mob mobToZap = mobsToZap.next();

                if (!attackedMobs.contains(mobToZap) && !hitMobs.contains(mobToZap)) {
                    ZombiesArena arena = getZombiesPlayer().getArena();
                    Vector unnormalized = mobToZap.getLocation().subtract(hitMob.getLocation()).toVector();
                    Vector normalized;
                    if(unnormalized.equals(ORIGIN)) {
                        normalized = new Vector(Vector.getEpsilon(), Vector.getEpsilon(), Vector.getEpsilon()).add(Vector.getRandom());
                    }
                    else {
                        normalized = unnormalized.normalize();
                    }

                    arena.getDamageHandler().damageEntity(getZombiesPlayer(), new ZapperAoeDamageAttempt(normalized), mobToZap);

                    attackedMobs.add(mobToZap);
                    counter++;
                }
            }
        }
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        super.damageEntity(rayTraceResult);

        Mob mob = (Mob)rayTraceResult.getHitEntity();

        //i think this should work; the mob is definitely damaged if != null
        if (mob != null) {
            hitMobs.add(mob);
        }
    }
}
