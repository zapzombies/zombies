package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class MapContextData extends EditorContextData {
    private final MapData map;

    public MapContextData(Player player, EditorContext context, MapData map) {
        super(player, context);

        this.map = map;
    }
}
