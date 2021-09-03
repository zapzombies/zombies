package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.game.data.map.WindowData;

public class NewWindowForm extends CommandForm<RoomSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("window"),
            new Parameter("create")
    };

    public NewWindowForm() {
        super("Creates a new window.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<RoomSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_ROOM_SELECTION;
    }

    @Override
    public String execute(Context context, Object[] arguments, RoomSelectionData data) {
        data.getRoom().getWindows().add(new WindowData(data.getPlayer().getWorld(), data.getSelection(),
                data.getPlayer().getLocation().toVector()));
        data.getContext().updateRenderable(EditorContext.Renderables.WINDOWS);
        return "Added window.";
    }
}
