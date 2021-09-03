package io.github.zap.zombies.game.data.map.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data for a shop
 */
@Getter
@AllArgsConstructor
public abstract class ShopData {

    private final String type;

    private final boolean requiresPower;

}
