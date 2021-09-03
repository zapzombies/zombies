package io.github.zap.zombies.game.equipment;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Object group of upgradeable equipments
 */
public abstract class UpgradeableEquipmentObjectGroup extends EquipmentObjectGroup {

    private final Map<String, Integer> equipmentLevelMap = new HashMap<>();

    @Getter
    private final boolean ultimateable, shouldSaveLevels;

    public UpgradeableEquipmentObjectGroup(@NotNull Player player, @NotNull Set<Integer> slots, boolean ultimateable,
                                           boolean shouldSaveLevels) {
        super(player, slots);

        this.ultimateable = ultimateable;
        this.shouldSaveLevels = shouldSaveLevels;
    }

    @Override
    public void remove(int slot, boolean replace) {
        if (shouldSaveLevels && replace) {
            HotbarObject hotbarObject = getHotbarObject(slot);

            if (hotbarObject instanceof UpgradeableEquipment<?, ?> upgradeableEquipment) {
                equipmentLevelMap.put(upgradeableEquipment.getEquipmentData().getName(),
                        upgradeableEquipment.getLevel());
            }
        }

        super.remove(slot, replace);
    }

    @Override
    public void setHotbarObject(int slot, @NotNull HotbarObject hotbarObject) {
        if (shouldSaveLevels && hotbarObject instanceof UpgradeableEquipment<?, ?> upgradeableEquipment) {
            Integer level = equipmentLevelMap.get(upgradeableEquipment.getEquipmentData().getName());
            if (level != null) {
                while (upgradeableEquipment.getLevel() < level) {
                    upgradeableEquipment.upgrade();
                }
            }
        }

        super.setHotbarObject(slot, hotbarObject);
    }

}
