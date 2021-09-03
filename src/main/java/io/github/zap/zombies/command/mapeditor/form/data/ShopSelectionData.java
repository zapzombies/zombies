package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Getter
public class ShopSelectionData extends RoomSelectionData {
    private final ShopType type;

    public ShopSelectionData(Player player, EditorContext context, BoundingBox bounds, MapData map, RoomData room,
                             ShopType type) {
        super(player, context, bounds, map, room);
        this.type = type;
    }
}
