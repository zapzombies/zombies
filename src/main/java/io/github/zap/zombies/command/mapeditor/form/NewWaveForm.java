package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Converters;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.game.data.map.WaveData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class NewWaveForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("wave"),
            new Parameter("create"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, Component.text("[round-index]"), Converters.INTEGER_CONVERTER)
    };

    private static final CommandValidator<MapContextData, MapContextData> validator = new CommandValidator<>(
            (context, objects, mapContextData) -> {
        int index = (int)objects[2];

        if(index >= mapContextData.getMap().getRounds().size()) {
            return ValidationResult.of(false, Component.text("Round index out of bounds!"), null);
        }

        return ValidationResult.of(true, null, mapContextData);
    }, MapeditorValidators.HAS_ACTIVE_MAP);

    public NewWaveForm(@NotNull RegularCommand command) {
        super(command, Component.text("Creates a new wave."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public Component execute(Context context, Object[] objects, MapContextData waveData) {
        int index = (int)objects[2];
        waveData.getMap().getRounds().get(index).getWaves().add(new WaveData());
        return Component.text("Added a new wave to round " + index + ".");
    }
}
