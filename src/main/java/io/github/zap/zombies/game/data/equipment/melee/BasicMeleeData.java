package io.github.zap.zombies.game.data.equipment.melee;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for a basic melee weapon
 */
public class BasicMeleeData extends MeleeData<BasicMeleeLevel> {

    public BasicMeleeData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                   @NotNull List<String> lore, @NotNull List<BasicMeleeLevel> levels) {
        super(EquipmentType.BASIC_MELEE.name(), name, displayName, material, lore, levels);
    }

    private BasicMeleeData() {

    }

}
