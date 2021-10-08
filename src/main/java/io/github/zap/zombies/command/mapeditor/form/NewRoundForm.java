package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.game.data.map.RoundData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class NewRoundForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("round"),
            new Parameter("create"),
    };

    public NewRoundForm(@NotNull RegularCommand command) {
        super(command, Component.text("Creates a new round."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] objects) {
        return MapeditorValidators.HAS_ACTIVE_MAP;
    }

    @Override
    public Component execute(Context context, Object[] objects, MapContextData waveData) {
        waveData.getMap().getRounds().add(new RoundData());
        return Component.text("Added a new round.");
    }
}
