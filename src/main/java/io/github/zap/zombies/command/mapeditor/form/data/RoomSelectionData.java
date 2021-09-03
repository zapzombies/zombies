package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Getter
public class RoomSelectionData extends MapSelectionData {
    private final RoomData room;

    public RoomSelectionData(Player player, EditorContext context, BoundingBox bounds, MapData map, RoomData room) {
        super(player, context, bounds, map);
        this.room = room;
    }
}
