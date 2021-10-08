package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.EditorContextData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ExitEditorForm extends CommandForm<EditorContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("exit", Component.text("exit"))
    };

    public ExitEditorForm(@NotNull RegularCommand command) {
        super(command, Component.text("Cancels a mapeditor session."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<EditorContextData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_EDITOR_CONTEXT;
    }

    @Override
    public Component execute(Context context, Object[] arguments, EditorContextData data) {
        data.getContext().setMap(null);
        data.getContext().dispose();
        Zombies.getInstance().getContextManager().removeContext(data.getPlayer());

        return Component.text("Ended mapeditor session.");
    }
}
