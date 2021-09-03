package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a perk machine
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class PerkMachineData extends BlockShopData {

    private String perkName = "DEFAULT";

    private List<Integer> costs = new ArrayList<>();

    private PerkMachineData() {
        super(ShopType.PERK_MACHINE.name(), true, null, null);
    }

    public PerkMachineData(@NotNull Vector blockLocation, @NotNull Vector hologramLocation) {
        super(ShopType.PERK_MACHINE.name(), true, blockLocation, hologramLocation);
    }

}
