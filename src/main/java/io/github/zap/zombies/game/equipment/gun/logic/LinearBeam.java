package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Sends lines of particles from guns
 */
@Getter
public class LinearBeam extends BasicBeam {

    public final static int DEFAULT_PARTICLE_COUNT = 4;

    private final Particle particle;
    private final ParticleDataWrapper<?> particleDataWrapper;
    private final int particleCount;


    public LinearBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, LinearGunLevel level,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper, int particleCount) {
        super(mapData, zombiesPlayer, root, level);

        this.particle = particle;
        this.particleDataWrapper = particleDataWrapper;
        this.particleCount = particleCount;
    }

    public LinearBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, LinearGunLevel level,
                      Particle particle, ParticleDataWrapper<?> particleDataWrapper) {
        this(mapData, zombiesPlayer, root, level, particle, particleDataWrapper, DEFAULT_PARTICLE_COUNT);
    }

    /**
     * Sends the bullet
     */
    public void send() {
        super.send();
        spawnParticles();
    }

    /**
     * Spawns the bullet's particles in a line
     */
    private void spawnParticles() {
        World world = getWorld();
        Location rootLocation = getRoot().toLocation(world);

        if (particleDataWrapper != null) {
            for (int i = 0; i < particleCount; i++) {
                world.spawnParticle(
                        particle,
                        rootLocation,
                        1,
                        0,
                        0,
                        0,
                        0,
                        particleDataWrapper.getData()
                );
                rootLocation.add(getDirectionVector());
            }
        } else {
            for (int i = 0; i < particleCount; i++) {
                world.spawnParticle(
                        particle,
                        rootLocation,
                        1,
                        0,
                        0,
                        0,
                        0
                );
                rootLocation.add(getDirectionVector());
            }
        }


    }

}
