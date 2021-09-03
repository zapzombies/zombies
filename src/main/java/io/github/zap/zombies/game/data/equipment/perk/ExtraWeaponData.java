package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the extra weapon perk
 */
public class ExtraWeaponData extends PerkData<ExtraWeaponLevel> {

    public ExtraWeaponData(@NotNull String name, @NotNull String displayName,
                           @NotNull Material material, @NotNull List<String> lore,
                           @NotNull List<ExtraWeaponLevel> levels) {
        super(EquipmentType.EXTRA_WEAPON.name(), name, displayName, material, lore, levels);
    }

    private ExtraWeaponData() {

    }

}
