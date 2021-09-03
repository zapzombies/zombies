package io.github.zap.zombies.game.equipment;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A piece of equipment that can be upgraded
 * @param <D> The type of equipment data the equipment uses
 * @param <L> The type of level the equipment uses
 */
public class UpgradeableEquipment<D extends EquipmentData<L>, L> extends Equipment<D, L> {

    @Getter
    private final int maxLevel;

    @Getter
    private int level = 0;

    public UpgradeableEquipment(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                                @NotNull D equipmentData) {
        super(arena, player, slot, equipmentData);

        this.maxLevel = equipmentData.getLevels().size() - 1;
    }

    /**
     * Upgrades the equipment
     */
    public void upgrade() {
        if (level < maxLevel) {
            setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), ++level));
        }
    }

    /**
     * Downgrades the equipment
     */
    public void downgrade() {
        if (level > 0) {
            setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), --level));
        }
    }

    @Override
    public @NotNull L getCurrentLevel() {
        return getEquipmentData().getLevels().get(level);
    }
}
