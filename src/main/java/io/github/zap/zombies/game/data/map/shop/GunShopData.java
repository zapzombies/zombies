package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a gun shop
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class GunShopData extends ArmorStandShopData {

    private String gunName = "NONE";

    private String gunDisplayName = "NONE";

    private int cost = 0;

    private int refillCost = 0;

    private GunShopData() {
        super(ShopType.GUN_SHOP.name(), false, null, null);
    }

    public GunShopData(@NotNull Vector rootLocation, @NotNull Vector hologramLocation) {
        super(ShopType.GUN_SHOP.name(), false, rootLocation, hologramLocation);
    }

}
