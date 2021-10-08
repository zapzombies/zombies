package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.util.Validators;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.game.data.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditMapForm extends CommandForm<MapContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("edit"),
            new Parameter(Regexes.OBJECT_NAME, Component.text("[name]"))
    };

    private static final CommandValidator<MapContextData, Player> validator =
            new CommandValidator<>((context, arguments, previous) -> {
        MapData map = Zombies.getInstance().getArenaManager().getMap((String)arguments[2]);

        if(map == null) {
            return ValidationResult.of(false, Component.text("That map does not exist!"), null);
        }

        return ValidationResult.of(true, null, new MapContextData(previous, Zombies.getInstance()
                .getContextManager().getContext(previous), map));
    }, Validators.PLAYER_EXECUTOR);

    public EditMapForm(@NotNull RegularCommand command) {
        super(command, Component.text("Edits an existing map."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapContextData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public Component execute(Context context, Object[] arguments, MapContextData data) {
        data.getContext().setMap(data.getMap());
        return Component.text("Now editing map '" + data.getMap().getName() + "'.");
    }
}
