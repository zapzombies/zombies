package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the frozen bullets perk
 */
public class FrozenBulletsData extends PerkData<FrozenBulletsLevel> {

    public FrozenBulletsData(@NotNull String name, @NotNull String displayName,
                             @NotNull Material material, @NotNull List<String> lore,
                             @NotNull List<FrozenBulletsLevel> levels) {
        super(EquipmentType.FROZEN_BULLETS.name(), name, displayName, material, lore, levels);
    }

    private FrozenBulletsData() {

    }

}
