package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.game.data.map.MapData;

public class SaveMapForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("save")
    };

    public SaveMapForm() {
        super("Saves the current map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_ACTIVE_MAP;
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public String execute(Context context, Object[] arguments, MapContextData data) {
        MapData mapData = data.getContext().getMap();
        Zombies.getInstance().getArenaManager().getMapLoader().save(mapData, mapData.getName());
        return "Saved map.";
    }
}
