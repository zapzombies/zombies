package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Data for an ultimate machine
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class UltimateMachineData extends BlockShopData {

    private int cost = 0;

    private UltimateMachineData() {
        super(ShopType.ULTIMATE_MACHINE.name(), true, null, null);
    }

    public UltimateMachineData(@NotNull Vector blockLocation, @NotNull Vector hologramLocation) {
        super(ShopType.ULTIMATE_MACHINE.name(), true, blockLocation, hologramLocation);
    }

}
