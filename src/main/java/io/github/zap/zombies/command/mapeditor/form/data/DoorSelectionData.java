package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Getter
public class DoorSelectionData extends MapSelectionData {
    private final DoorData door;

    public DoorSelectionData(Player player, EditorContext context, BoundingBox bounds, MapData map, DoorData door) {
        super(player, context, bounds, map);
        this.door = door;
    }
}
