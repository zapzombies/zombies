package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.WaveData;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class SpawnEntryContextData extends MapContextData {
    private final WaveData wave;
    private final SpawnEntryData spawnEntry;

    public SpawnEntryContextData(Player player, EditorContext context, MapData map, WaveData wave, SpawnEntryData spawnEntry) {
        super(player, context, map);
        this.wave = wave;
        this.spawnEntry = spawnEntry;
    }
}
