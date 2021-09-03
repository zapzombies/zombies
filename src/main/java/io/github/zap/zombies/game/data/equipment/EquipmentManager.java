package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Stores and manages information about equipments
 */
public interface EquipmentManager {

    /**
     * Adds a piece of equipment mapping
     * @param equipmentType The equipmentType key of the equipment type
     * @param dataClass The class of the data used to create the equipment
     * @param equipmentMapping A mapping class used to create the equipment from a data instance
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used by the equipment
     */
    <D extends EquipmentData<L>, L> void addEquipmentType(@NotNull String equipmentType, @NotNull Class<D> dataClass,
                                                          @NotNull EquipmentCreator
                                                                  .EquipmentMapping<D, L> equipmentMapping);
    /**
     * Gets a piece of equipment data
     * @param mapName The name of the map the equipment will be used in
     * @param name The name that the equipment data belongs to
     * @return The equipment data
     */
    @Nullable EquipmentData<?> getEquipmentData(@NotNull String mapName, @NotNull String name);

    /**
     * Creates a piece of equipment
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used for the equipment
     * @param arena The zombies arena to create the equipment in
     * @param player The player to create the equipment for
     * @param slot The slot the equipment will go in
     * @param mapName The name of the map the equipment will be used in
     * @param name The name key of the equipment
     * @return The new piece of equipment
     */
    @NotNull <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(@NotNull ZombiesArena arena,
                                                                             @NotNull ZombiesPlayer player,
                                                                             int slot, @NotNull String mapName,
                                                                             @NotNull String name);

    /**
     * Creates a piece of equipment
     * @param arena The zombies arena to create the equipment in
     * @param player The player to create the equipment for
     * @param slot The slot the equipment will go in
     * @param equipmentData The equipment data to create the equipment with
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used for the equipment
     * @return The new piece of equipment
     */
    @NotNull <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(@NotNull ZombiesArena arena,
                                                                             @NotNull ZombiesPlayer player,
                                                                             int slot,
                                                                             @NotNull D equipmentData);

    /**
     * Creates an equipment object group based on its equipment type
     * @param equipmentType The string representation of the equipment type
     * @param player The player to create the equipment object group for
     * @param slots The slots allocated for the equipment object group
     * @return The new equipment object group
     */
    @NotNull EquipmentObjectGroup createEquipmentObjectGroup(@NotNull String equipmentType, @NotNull Player player,
                                                             @NotNull Set<Integer> slots);
}
