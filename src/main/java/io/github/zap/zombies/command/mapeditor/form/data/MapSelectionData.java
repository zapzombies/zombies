package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Getter
public class MapSelectionData extends BoundsContextData {
    private final MapData map;

    public MapSelectionData(Player player, EditorContext context, BoundingBox bounds, MapData map) {
        super(player, context, bounds);
        this.map = map;
    }
}
