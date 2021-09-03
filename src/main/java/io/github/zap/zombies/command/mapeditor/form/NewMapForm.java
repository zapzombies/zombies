package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.BoundsContextData;
import io.github.zap.zombies.game.data.map.MapData;

public class NewMapForm extends CommandForm<BoundsContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("create"),
            new Parameter(Regexes.OBJECT_NAME, "[name]")
    };

    private static final CommandValidator<BoundsContextData, BoundsContextData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
        if(Zombies.getInstance().getArenaManager().hasMap((String)arguments[2])) {
            return ValidationResult.of(false, "A map with that name already exists.", null);
        }

        return ValidationResult.of(true, null, previousData);
    }, MapeditorValidators.HAS_SELECTION);

    public NewMapForm() {
        super("Creates a new map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<BoundsContextData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, BoundsContextData data) {
        EditorContext editorContext = data.getContext();
        MapData map = new MapData((String)arguments[2], data.getPlayer().getWorld().getName(), data.getSelection());
        Zombies.getInstance().getArenaManager().addMap(map);
        editorContext.setMap(map);
        editorContext.updateRenderable(EditorContext.Renderables.MAP);
        return String.format("Created new map '%s' in world %s", map.getName(), map.getWorldName());
    }
}
