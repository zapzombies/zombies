package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the flaming bullets perk
 */
public class FlamingBulletsData extends PerkData<FlamingBulletsLevel> {

    public FlamingBulletsData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                              @NotNull List<String> lore, @NotNull List<FlamingBulletsLevel> levels) {
        super(EquipmentType.FROZEN_BULLETS.name(), name, displayName, material, lore, levels);
    }

    private FlamingBulletsData() {

    }

}
