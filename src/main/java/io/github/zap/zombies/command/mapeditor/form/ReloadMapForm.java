package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.EditorContextData;
import io.github.zap.zombies.game.data.map.MapData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ReloadMapForm extends CommandForm<EditorContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("reload")
    };

    public ReloadMapForm(@NotNull RegularCommand command) {
        super(command, Component.text("Reloads all MapData."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<EditorContextData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_EDITOR_CONTEXT;
    }

    @Override
    public Component execute(Context context, Object[] arguments, EditorContextData data) {
        try {
            Zombies zombies = Zombies.getInstance();
            zombies.getArenaManager().loadMaps();

            for(EditorContext editorContext : zombies.getContextManager().getContexts()) {
                MapData current = editorContext.getMap();
                if(zombies.getArenaManager().hasMap(current.getName())) {
                    editorContext.setMap(zombies.getArenaManager().getMap(current.getName()));
                }
                else {
                    editorContext.setMap(null);
                }
            }
        }
        catch (LoadFailureException e) {
            return Component.text(e.getMessage());
        }

        return Component.text("Reloaded all maps.");
    }
}
