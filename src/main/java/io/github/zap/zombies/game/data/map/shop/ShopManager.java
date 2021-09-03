package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import io.github.zap.zombies.game.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Stores and manages information about shops
 */
public interface ShopManager {

    /**
     * Adds a shop mapping
     * @param shopType The string representation of the shop type
     * @param dataClass The class of the data the shop uses
     * @param shopMapping A mapping class used to create the shop from the data instance
     * @param <D> The type of the shop's data
     */
    <D extends ShopData> void addShop(@NotNull String shopType, @NotNull Class<D> dataClass,
                                      @NotNull ShopCreator.ShopMapping<D> shopMapping);

    /**
     * Adds a team machine task
     * @param type The string representation of the team machine task type
     * @param clazz The class of the data the team machine task uses for data and execution
     */
    void addTeamMachineTask(@NotNull String type, @NotNull Class<? extends TeamMachineTask> clazz);

    /**
     * Creates a shop from its data
     * @param arena The arena to create the shop for
     * @param shopData The shop's data
     * @param <D> The type of the shop's data
     * @return The new shop
     */
    @NotNull <D extends ShopData> Shop<D> createShop(@NotNull ZombiesArena arena, @NotNull D shopData);

}
