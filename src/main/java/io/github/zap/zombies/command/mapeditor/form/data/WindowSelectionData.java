package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.WindowData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Getter
public class WindowSelectionData extends RoomSelectionData {
    private final WindowData window;

    public WindowSelectionData(Player player, EditorContext context, BoundingBox bounds, MapData map, RoomData room,
                               WindowData window) {
        super(player, context, bounds, map, room);
        this.window = window;
    }
}
