package io.github.zap.zombies.game.data.map.shop;

import lombok.Getter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data for a block shop
 */
@Getter
public abstract class BlockShopData extends ShopData {
    private final Vector blockLocation;

    private final Vector hologramLocation;

    public BlockShopData(@NotNull String type, boolean requiresPower, @Nullable Vector blockLocation,
                         @Nullable Vector hologramLocation) {
        super(type, requiresPower);
        this.blockLocation = blockLocation;
        this.hologramLocation = hologramLocation;
    }
}
