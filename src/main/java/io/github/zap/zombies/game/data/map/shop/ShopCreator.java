package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.shop.Shop;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that stores a map of string shop names and a shop mapping in order to create shops from shop data
 */
public class ShopCreator {

    @Getter
    private final Map<String, ShopMapping<?>> shopMappings = new HashMap<>();

    /**
     * Interface to create a shop from its respective data class
     * @param <D> The data class of the shop
     */
    @FunctionalInterface
    public interface ShopMapping<D extends ShopData> {
        /**
         * Creates a shop
         * @param zombiesArena The arena of the shop
         * @param shopData The shop's data
         * @return The new shop
         */
        Shop<D> createShop(ZombiesArena zombiesArena, D shopData);
    }

}
