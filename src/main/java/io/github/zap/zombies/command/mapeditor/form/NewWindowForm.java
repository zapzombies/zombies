package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.game.data.map.WindowData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class NewWindowForm extends CommandForm<RoomSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("window", Component.text("window")),
            new Parameter("create", Component.text("create"))
    };

    public NewWindowForm(@NotNull RegularCommand command) {
        super(command, Component.text("Creates a new window."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<RoomSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_ROOM_SELECTION;
    }

    @Override
    public Component execute(Context context, Object[] arguments, RoomSelectionData data) {
        data.getRoom().getWindows().add(new WindowData(data.getPlayer().getWorld(), data.getSelection(),
                data.getPlayer().getLocation().toVector()));
        data.getContext().updateRenderable(EditorContext.Renderables.WINDOWS);
        return Component.text("Added window.");
    }
}
