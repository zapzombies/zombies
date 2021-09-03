package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.EditorContextData;

public class ExitEditorForm extends CommandForm<EditorContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("exit")
    };

    public ExitEditorForm() {
        super("Cancels a mapeditor session.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<EditorContextData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_EDITOR_CONTEXT;
    }

    @Override
    public String execute(Context context, Object[] arguments, EditorContextData data) {
        data.getContext().setMap(null);
        data.getContext().dispose();
        Zombies.getInstance().getContextManager().removeContext(data.getPlayer());

        return "Ended mapeditor session.";
    }
}
