package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.equipment.gun.SprayGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Mob;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

/**
 * A single pellet of a spray gun's beam
 */
public class SprayShotBeam extends LinearBeam {

    private class SprayShotDamageAttempt extends BeamDamageAttempt {

        private final int divisionFactor;

        public SprayShotDamageAttempt(boolean isHeadshot, int factor) {
            super(isHeadshot);
            this.divisionFactor = factor;
        }

        @Override
        public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
            return super.knockbackFactor(damager, target) / divisionFactor;
        }
    }

    private final int pellets;

    public SprayShotBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, SprayGunLevel level,
                         Particle particle, ParticleDataWrapper<?> particleDataWrapper, int particleCount,
                         int pellets) {
        super(mapData, zombiesPlayer, root, level, particle, particleDataWrapper, particleCount);
        this.pellets = pellets;
    }

    public SprayShotBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, LinearGunLevel level,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper, int pellets) {
        super(mapData, zombiesPlayer, root, level, particle, particleDataWrapper);
        this.pellets = pellets;
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            ZombiesArena arena = getZombiesPlayer().getArena();
            arena.getDamageHandler().damageEntity(getZombiesPlayer(),
                    new SprayShotDamageAttempt(determineIfHeadshot(rayTraceResult, mob), pellets), mob);
        }
    }
}
