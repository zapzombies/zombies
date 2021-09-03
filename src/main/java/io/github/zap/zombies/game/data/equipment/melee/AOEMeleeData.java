package io.github.zap.zombies.game.data.equipment.melee;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for an AOE melee weapon
 */
public class AOEMeleeData extends MeleeData<AOEMeleeLevel> {

    public AOEMeleeData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                        @NotNull List<String> lore, @NotNull List<AOEMeleeLevel> levels) {
        super(EquipmentType.AOE_MELEE.name(), name, displayName, material, lore, levels);
    }


    private AOEMeleeData() {

    }

}
