package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the fast revive perk
 */
public class FastReviveData extends PerkData<FastReviveLevel> {

    public FastReviveData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                          @NotNull List<String> lore, @NotNull List<FastReviveLevel> levels) {
        super(EquipmentType.FAST_REVIVE.name(), name, displayName, material, lore, levels);
    }

    private FastReviveData() {

    }

}
