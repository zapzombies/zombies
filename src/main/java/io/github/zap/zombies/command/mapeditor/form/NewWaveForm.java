package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.game.data.map.WaveData;

public class NewWaveForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("wave"),
            new Parameter("create"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[round-index]", Converters.INTEGER_CONVERTER)
    };

    private static final CommandValidator<MapContextData, MapContextData> validator = new CommandValidator<>((context, objects, mapContextData) -> {
        int index = (int)objects[2];

        if(index >= mapContextData.getMap().getRounds().size()) {
            return ValidationResult.of(false, "Round index out of bounds!", null);
        }

        return ValidationResult.of(true, null, mapContextData);
    }, MapeditorValidators.HAS_ACTIVE_MAP);

    public NewWaveForm() {
        super("Creates a new wave.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] objects, MapContextData waveData) {
        int index = (int)objects[2];
        waveData.getMap().getRounds().get(index).getWaves().add(new WaveData());
        return "Added a new wave to round " + index + ".";
    }
}
