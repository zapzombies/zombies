package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a door
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class DoorData extends ShopData {

    private MultiBoundingBox doorBounds = new MultiBoundingBox();

    private List<DoorSide> doorSides = new ArrayList<>();

    private Sound openSound = Sound.sound(org.bukkit.Sound.BLOCK_WOODEN_DOOR_OPEN.getKey(), Sound.Source.BLOCK, 2.0F, 1.0F);

    public DoorData() {
        super(ShopType.DOOR.name(), false);
    }

}
