package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * Data for a power switch
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class PowerSwitchData extends BlockShopData {

    private int cost = 0;

    private PowerSwitchData() {
        super(ShopType.POWER_SWITCH.name(), false, null, null);
    }

    public PowerSwitchData(Vector blockLocation, Vector hologramLocation) {
        super(ShopType.POWER_SWITCH.name(), false, blockLocation, hologramLocation);
    }
    
}
