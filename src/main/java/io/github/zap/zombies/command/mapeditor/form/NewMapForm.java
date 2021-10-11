package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.BoundsContextData;
import io.github.zap.zombies.game.data.map.MapData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class NewMapForm extends CommandForm<BoundsContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map", Component.text("map")),
            new Parameter("create", Component.text("create")),
            new Parameter(Regexes.OBJECT_NAME, Component.text("[name]"), false)
    };

    private static final CommandValidator<BoundsContextData, BoundsContextData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
        if(Zombies.getInstance().getArenaManager().hasMap((String)arguments[2])) {
            return ValidationResult.of(false, Component.text("A map with that name already exists."), null);
        }

        return ValidationResult.of(true, null, previousData);
    }, MapeditorValidators.HAS_SELECTION);

    public NewMapForm(@NotNull RegularCommand command) {
        super(command, Component.text("Creates a new map."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<BoundsContextData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public Component execute(Context context, Object[] arguments, BoundsContextData data) {
        EditorContext editorContext = data.getContext();
        MapData map = new MapData((String)arguments[2], data.getPlayer().getWorld().getName(), data.getSelection());
        Zombies.getInstance().getArenaManager().addMap(map);
        editorContext.setMap(map);
        editorContext.updateRenderable(EditorContext.Renderables.MAP);
        return Component.text("Created new map " + map.getName() + " in world " + map.getWorldName());
    }
}
