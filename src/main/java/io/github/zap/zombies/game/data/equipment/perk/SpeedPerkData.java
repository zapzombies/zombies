package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the speed perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class SpeedPerkData extends PerkData<SpeedPerkLevel> {

    private int speedReapplyInterval = 500;

    public SpeedPerkData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                         @NotNull List<String> lore, @NotNull List<SpeedPerkLevel> levels) {
        super(EquipmentType.SPEED.name(), name, displayName, material, lore, levels);
    }

    private SpeedPerkData() {

    }

}
