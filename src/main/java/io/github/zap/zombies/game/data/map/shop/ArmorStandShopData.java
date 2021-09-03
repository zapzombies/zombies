package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data for an armor stand shop
 */
@Getter
public abstract class ArmorStandShopData extends ShopData {

    private final Vector rootLocation;

    private final Vector hologramLocation;

    public ArmorStandShopData(@NotNull String type, boolean requiresPower, @Nullable Vector rootLocation,
                              @Nullable Vector hologramLocation) {
        super(type, requiresPower);
        this.rootLocation = rootLocation;
        this.hologramLocation = hologramLocation;
    }

}
