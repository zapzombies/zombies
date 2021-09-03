package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.util.ParticleDataWrapper;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Data for a gun associated with a particle
 * @param <L> The gun level type
 */
@Getter
public abstract class ParticleGunData<L extends ParticleGunLevel> extends GunData<L> {

    private Particle particle;

    private final ParticleDataWrapper<?> particleDataWrapper;

    public ParticleGunData(@NotNull String type, @NotNull String name, @NotNull String displayName,
                           @NotNull Material material, @NotNull List<String> lore, @NotNull List<L> levels,
                           @NotNull Particle particle, @Nullable ParticleDataWrapper<?> particleDataWrapper) {
        super(type, name, displayName, material, lore, levels);

        this.particle = particle;
        this.particleDataWrapper = particleDataWrapper;
    }

    protected ParticleGunData() {
        this.particleDataWrapper = null;
    }

}
