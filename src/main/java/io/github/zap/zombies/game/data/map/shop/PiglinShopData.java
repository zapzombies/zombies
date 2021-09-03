package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for a piglin shop
 */
@Getter
@Setter
public class PiglinShopData extends ShopData {

    private Vector piglinLocation;

    private List<String> equipments;

    private float direction = 0.0F;

    private long sittingTime = 200;

    private int cost = 1000;

    public PiglinShopData(@NotNull Vector piglinLocation, boolean requiresPower) {
        super(ShopType.PIGLIN_SHOP.name(), requiresPower);

        this.piglinLocation = piglinLocation;
    }

    public PiglinShopData() {
        super(ShopType.PIGLIN_SHOP.name(), false);
    }

    public PiglinShopData(Vector piglinLocation) {
        super(ShopType.PIGLIN_SHOP.name(), false);

        this.piglinLocation = piglinLocation;
    }

}
