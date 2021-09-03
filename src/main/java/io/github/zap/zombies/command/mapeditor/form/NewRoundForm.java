package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.game.data.map.RoundData;

public class NewRoundForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("round"),
            new Parameter("create"),
    };

    public NewRoundForm() {
        super("Creates a new round.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] objects) {
        return MapeditorValidators.HAS_ACTIVE_MAP;
    }

    @Override
    public String execute(Context context, Object[] objects, MapContextData waveData) {
        waveData.getMap().getRounds().add(new RoundData());
        return "Added a new round. Total rounds: " + waveData.getMap().getRounds().size();
    }
}
