package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class NewSpawnpointForm extends CommandForm<RoomSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("spawn"),
            new Parameter("create"),
            new Parameter(Regexes.OBJECT_NAME, Component.text("[spawn-rule-name]"))
    };

    private static final CommandValidator<RoomSelectionData, RoomSelectionData> validator =
            new CommandValidator<>((context, objects, roomSelectionData) -> {
        //noinspection SuspiciousMethodCalls
        if(!roomSelectionData.getMap().getSpawnRules().containsKey(objects[2])) {
            return ValidationResult.of(false, Component.text("A spawnrule with that name does not exist!"),
                    null);
        }

        return ValidationResult.of(true, null, roomSelectionData);
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public NewSpawnpointForm(@NotNull RegularCommand command) {
        super(command, Component.text("Creates a new spawnpoint in a room or window."), Permissions.OPERATOR,
                parameters);
    }

    @Override
    public CommandValidator<RoomSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public Component execute(Context context, Object[] arguments, RoomSelectionData data) {
        SpawnpointData spawnpointData = new SpawnpointData(data.getContext().getTarget(), (String)arguments[2]);
        for(WindowData windowData : data.getRoom().getWindows()) {
            if(windowData.getInteriorBounds().contains(data.getSelection())) {
                windowData.getSpawnpoints().add(spawnpointData);
                data.getContext().updateRenderable(EditorContext.Renderables.SPAWNPOINTS);
                return Component.text("Added spawnpoint to window.");
            }
        }

        data.getRoom().getSpawnpoints().add(spawnpointData);
        data.getContext().updateRenderable(EditorContext.Renderables.SPAWNPOINTS);
        return Component.text("Added spawnpoint to room.");
    }
}
