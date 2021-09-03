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
import io.github.zap.zombies.command.mapeditor.form.data.DoorSelectionData;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.game.data.map.shop.DoorData;

import java.util.List;

public class NewDoorForm extends CommandForm<DoorSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("door"),
            new Parameter("addbounds"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[door-index]", Converters.INTEGER_CONVERTER)
    };

    private static final CommandValidator<DoorSelectionData, MapSelectionData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
        int index = (int)arguments[2];

        List<DoorData> doors = previousData.getMap().getDoors();
        if(index > doors.size()) {
            return ValidationResult.of(false, "Index out of bounds!", null);
        }

        DoorData door;
        if(index == doors.size()) {
            door = new DoorData();
            previousData.getMap().getDoors().add(door);
        }
        else {
            door = doors.get(index);
        }

        return ValidationResult.of(true, null, new DoorSelectionData(previousData.getPlayer(),
                previousData.getContext(), previousData.getSelection(), previousData.getMap(), door));
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public NewDoorForm() {
        super("Creates a new door.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<DoorSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, DoorSelectionData data) {
        data.getDoor().getDoorBounds().addBounds(data.getSelection());
        data.getContext().updateRenderable(EditorContext.Renderables.DOORS);
        return "Added door bounds.";
    }
}