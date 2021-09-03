package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents data for a guardian gun
 */
public class GuardianGunData extends GunData<GuardianGunLevel> {

    public GuardianGunData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                   @NotNull List<String> lore, @NotNull List<GuardianGunLevel> levels) {
        super(EquipmentType.GUARDIAN.name(), name, displayName, material, lore, levels);
    }

    private GuardianGunData() {

    }

}
