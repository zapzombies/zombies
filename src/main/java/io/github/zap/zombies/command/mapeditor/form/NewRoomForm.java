package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;

import java.util.List;

public class NewRoomForm extends CommandForm<MapSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("room"),
            new Parameter("addbounds"),
            new Parameter(Regexes.OBJECT_NAME, "[name]")
    };

    private static final CommandValidator<MapSelectionData, MapSelectionData> validator = new CommandValidator<>((context, arguments, previousData) -> {
        MapData map = previousData.getMap();
        List<RoomData> rooms = map.getRooms();

        for(RoomData room : rooms) {
            //allow overlapping bounds for same room, but not bounds from different rooms
            if(!room.getName().equals(arguments[2]) && room.getBounds().overlaps(previousData.getSelection())) {
                return ValidationResult.of(false, "You cannot overlap one room's bounds with another!", null);
            }
        }

        return ValidationResult.of(true, null, previousData);
    }, MapeditorValidators.HAS_MAP_SELECTION);

    public NewRoomForm() {
        super("Creates a new room, or adds bounds to an existing one.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<MapSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, MapSelectionData data) {
        String name = (String)arguments[2];

        for(RoomData room : data.getMap().getRooms()) {
            if(room.getName().equals(name)) {
                room.getBounds().addBounds(data.getSelection());
                data.getContext().updateRenderable(EditorContext.Renderables.ROOMS);
                return "Added new bounds to room.";
            }
        }

        RoomData newData = new RoomData(name);
        newData.getBounds().addBounds(data.getSelection());
        data.getMap().getRooms().add(newData);
        data.getContext().updateRenderable(EditorContext.Renderables.ROOMS);
        return "Created new room.";
    }
}
