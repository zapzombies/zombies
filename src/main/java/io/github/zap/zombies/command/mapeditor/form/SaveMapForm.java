package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.game.data.map.MapData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class SaveMapForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map", Component.text("map")),
            new Parameter("save", Component.text("save"))
    };

    public SaveMapForm(@NotNull RegularCommand command) {
        super(command, Component.text("Saves the current map."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_ACTIVE_MAP;
    }

    @Override
    public Component execute(Context context, Object[] arguments, MapContextData data) {
        MapData mapData = data.getContext().getMap();
        Zombies.getInstance().getArenaManager().getMapLoader().save(mapData, mapData.getName());
        return Component.text("Saved map.");
    }
}
