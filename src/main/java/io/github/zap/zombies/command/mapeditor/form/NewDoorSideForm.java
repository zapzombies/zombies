package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.ConversionResult;
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
import io.github.zap.zombies.game.data.map.shop.DoorSide;

import java.util.Arrays;
import java.util.List;

public class NewDoorSideForm extends CommandForm<DoorSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("door"),
            new Parameter("side"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[door-index]", Converters.INTEGER_CONVERTER),
            new Parameter(Regexes.STRING_LIST, "[opens-to]", Converters.newArrayConverter(argument ->
                    ConversionResult.of(true, argument, null), ",", String.class))
    };

    private static final CommandValidator<DoorSelectionData, MapSelectionData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
        int index = (int)arguments[2];

        List<DoorData> doors = previousData.getMap().getDoors();

        if(index >= doors.size()) {
            return ValidationResult.of(false, "Index out of bounds!", null);
        }

        DoorData targetDoor = doors.get(index);
        for(DoorData door : doors) {
            for(DoorSide side : door.getDoorSides()) {
                if(side.getTriggerBounds().overlaps(previousData.getSelection())) {
                    return ValidationResult.of(false,
                            "Trigger bounds cannot overlap other triggers!", null);
                }
            }
        }

        String[] opensTo = (String[])arguments[3];
        for(String roomName : opensTo) {
            if(previousData.getMap().getRooms().stream().noneMatch(roomData -> roomData.getName().equals(roomName))) {
                return ValidationResult.of(false, "Room " + roomName + " does not exist!", null);
            }
        }

        return ValidationResult.of(true, null, new DoorSelectionData(previousData.getPlayer(),
                previousData.getContext(), previousData.getSelection(), previousData.getMap(), targetDoor));
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public NewDoorSideForm() {
        super("Creates a new door side.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<DoorSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, DoorSelectionData data) {
        DoorData door = data.getDoor();
        DoorSide newSide = new DoorSide(0, Arrays.asList((String[])arguments[3]), data.getSelection(),
                data.getPlayer().getLocation().toVector());
        door.getDoorSides().add(newSide);
        data.getContext().updateRenderable(EditorContext.Renderables.DOOR_SIDES);
        return "Added new door side.";
    }
}
