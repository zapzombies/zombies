package io.github.zap.zombies.game.equipment;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class that stores a map of string names and an equipment object group mapping in order to get an
 * equipment object group from its equipment type string
 */
public class EquipmentObjectGroupCreator {

    @Getter
    public Map<String, EquipmentObjectGroupMapping> equipmentObjectGroupMappings = new HashMap<>();

    /**
     * Interface used to create an equipment object group from its equipment type string
     */
    public interface EquipmentObjectGroupMapping {

        /**
         * Creates an equipment object group
         * @param player The player to create the equipment object group for
         * @param slots The slots used by the equipment object group
         * @return The new equipment object group
         */
        @NotNull EquipmentObjectGroup createEquipmentObjectGroup(@NotNull Player player, @NotNull Set<Integer> slots);
    }

}
