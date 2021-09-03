package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for an armor shop
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class ArmorShopData extends ArmorStandShopData {

    private float armorStandDirection;

    private List<ArmorLevel> armorLevels = new ArrayList<>();

    private ArmorShopData() {
        super(ShopType.ARMOR_SHOP.name(), false, null, null);
    }

    public ArmorShopData(Vector rootLocation, Vector hologramLocation) {
        super(ShopType.ARMOR_SHOP.name(), false, rootLocation, hologramLocation);
    }

    /**
     * A level of an armor shop's available armor levels
     */
    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    public static class ArmorLevel {

        private String name = "default";

        private int cost = 0;

        private Material[] materials = new Material[0];

        private ArmorLevel() {

        }

    }

}
