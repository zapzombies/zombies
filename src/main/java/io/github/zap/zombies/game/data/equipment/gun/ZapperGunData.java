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
 * Data for a zapper gun
 */
@Getter
public class ZapperGunData extends ParticleGunData<ZapperGunLevel> {

    public ZapperGunData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                         @NotNull List<String> lore, @NotNull List<ZapperGunLevel> levels, @NotNull Particle particle,
                         @Nullable ParticleDataWrapper<?> particleDataWrapper) {
        super(EquipmentType.ZAPPER.name(), name, displayName, material, lore, levels, particle, particleDataWrapper);
    }

    private ZapperGunData() {

    }

}
