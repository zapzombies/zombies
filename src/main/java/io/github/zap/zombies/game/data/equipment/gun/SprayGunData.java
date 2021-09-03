package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Data for a spray gun
 */
@Getter
public class SprayGunData extends ParticleGunData<SprayGunLevel> {

    public SprayGunData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                        @NotNull List<String> lore, @NotNull List<SprayGunLevel> levels, @NotNull Particle particle,
                        @Nullable ParticleDataWrapper<?> particleDataWrapper) {
        super(EquipmentType.SPRAY_GUN.name(), name, displayName, material, lore, levels, particle, particleDataWrapper);
    }

    private SprayGunData() {

    }

}
