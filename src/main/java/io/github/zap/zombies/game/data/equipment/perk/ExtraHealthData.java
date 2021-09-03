package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the extra health perk
 */
public class ExtraHealthData extends PerkData<ExtraHealthLevel> {

    public ExtraHealthData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                           @NotNull List<String> lore, @NotNull List<ExtraHealthLevel> levels) {
        super(EquipmentType.EXTRA_HEALTH.name(), name, displayName, material, lore, levels);
    }

    private ExtraHealthData() {

    }

}
