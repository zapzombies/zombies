package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the quick fire perk
 */
public class QuickFireData extends PerkData<QuickFireLevel> {

    public QuickFireData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                         @NotNull List<String> lore, @NotNull List<QuickFireLevel> levels) {
        super(EquipmentType.QUICK_FIRE.name(), name, displayName, material, lore, levels);
    }

    private QuickFireData() {

    }

}
