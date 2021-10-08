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
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.game.data.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeleteMapForm extends CommandForm<MapData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("delete"),
            new Parameter(Regexes.OBJECT_NAME, Component.text("[map-name]"))
    };

    private static final CommandValidator<MapData, Player> validator = new CommandValidator<>(
            (context, arguments, previousData) ->{
        String arg = (String)arguments[2];
        if(!Zombies.getInstance().getArenaManager().hasMap(arg)) {
            return ValidationResult.of(false, Component.text("That map does not exist!"), null);
        }

        return ValidationResult.of(true, null, Zombies.getInstance().getArenaManager().getMap(arg));
    }, Validators.PLAYER_EXECUTOR);

    public DeleteMapForm(@NotNull RegularCommand command) {
        super(command, Component.text("Deletes an existing map."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public @NotNull Component execute(Context context, Object[] arguments, MapData data) {
        Zombies.getInstance().getArenaManager().deleteOnDisable(data.getName());

        for(EditorContext editorContext : Zombies.getInstance().getContextManager().getContexts()) {
            if (editorContext.getMap() == data) {
                editorContext.setMap(null);
            }
        }

        return Component.text("Deleted map.");
    }
}
