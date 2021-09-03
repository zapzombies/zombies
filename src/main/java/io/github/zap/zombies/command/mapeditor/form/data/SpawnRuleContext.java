package io.github.zap.zombies.command.mapeditor.form.data;

import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.SpawnRule;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class SpawnRuleContext extends MapContextData {
    private final SpawnRule rule;

    public SpawnRuleContext(Player player, EditorContext context, MapData map, SpawnRule rule) {
        super(player, context, map);
        this.rule = rule;
    }
}
