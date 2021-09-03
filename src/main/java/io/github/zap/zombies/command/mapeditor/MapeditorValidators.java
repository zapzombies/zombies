package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.form.data.*;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class MapeditorValidators {
    public static final CommandValidator<EditorContextData, Player> HAS_EDITOR_CONTEXT =
            new CommandValidator<>((context, arguments, previousData) -> {
        Zombies zombies = Zombies.getInstance();
        EditorContext editorContext = zombies.getContextManager().getContext(previousData);
        return ValidationResult.of(true, null, new EditorContextData(previousData, editorContext));
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator<EditorContextData, EditorContextData> NO_ACTIVE_MAP =
            new CommandValidator<>((context, arguments, previousData) -> {
        if(previousData.getContext().getMap() != null) {
            return ValidationResult.of(false, "You are already editing a map.", null);
        }

        return ValidationResult.of(true, null, previousData);
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<MapContextData, EditorContextData>
            HAS_ACTIVE_MAP = new CommandValidator<>((context, arguments, previousData) -> {
        MapData map = previousData.getContext().getMap();
        if(map == null) {
            return ValidationResult.of(false, "You are not editing a map.", null);
        }

        return ValidationResult.of(true, null, new MapContextData(previousData.getPlayer(), previousData.getContext(), map));
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<BoundsContextData, EditorContextData>
            HAS_SELECTION = new CommandValidator<>((context, arguments, previousData) -> {
        BoundingBox selection = previousData.getContext().getSelection();
        if(selection == null) {
            return ValidationResult.of(false, "You must have something selected to use this command.", null);
        }

        return ValidationResult.of(true, null, new BoundsContextData(previousData.getPlayer(), previousData.getContext(), selection));
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<MapSelectionData, BoundsContextData> HAS_MAP_SELECTION =
            new CommandValidator<>((context, arguments, previousData) ->
                    ValidationResult.of(true, null, new MapSelectionData(previousData.getPlayer(),
                            previousData.getContext(), previousData.getSelection(), previousData.getContext().getMap())),
                    HAS_SELECTION.from(HAS_ACTIVE_MAP));

    public static final CommandValidator<RoomSelectionData, MapSelectionData> HAS_ROOM_SELECTION =
            new CommandValidator<>((context, arguments, previousData) -> {
                BoundingBox selection = previousData.getSelection();

                for(RoomData room : previousData.getMap().getRooms()) {
                    if(room.getBounds().contains(selection)) {
                        return ValidationResult.of(true, null, new RoomSelectionData(previousData.getPlayer(),
                                previousData.getContext(), previousData.getSelection(), previousData.getMap(), room));
                    }
                }

                return ValidationResult.of(false, "Your selection must be contained in a room!", null);
            }, MapeditorValidators.HAS_MAP_SELECTION);
}
