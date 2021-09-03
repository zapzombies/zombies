package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.command.mapeditor.form.data.WindowSelectionData;
import io.github.zap.zombies.game.data.map.RoomData;

public class WindowBoundsForm extends CommandForm<WindowSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("window"),
            new Parameter("addbounds"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[target-index]", Converters.INTEGER_CONVERTER)
    };

    private static final CommandValidator<WindowSelectionData, RoomSelectionData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
        RoomData room = previousData.getRoom();

        int index = (int)arguments[2];
        int windowCount = room.getWindows().size();

        if(index >= windowCount) {
            return ValidationResult.of(false, "Index out of bounds! Number of windows: " + windowCount, null);
        }

        return ValidationResult.of(true, null, new WindowSelectionData(previousData.getPlayer(),
                previousData.getContext(), previousData.getSelection(), previousData.getMap(), previousData.getRoom(),
                room.getWindows().get(index)));
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public WindowBoundsForm() {
        super("Adds a bounds to a certain target window.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<WindowSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, WindowSelectionData data) {
        data.getWindow().getInteriorBounds().addBounds(data.getSelection());
        data.getContext().updateRenderable(EditorContext.Renderables.WINDOW_BOUNDS);
        return "Added new window interior bounds.";
    }
}
