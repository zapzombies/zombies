package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.StringUtils;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.form.data.EditorContextData;
import io.github.zap.zombies.game.data.map.MapData;

public class ReloadMapForm extends CommandForm<EditorContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("reload")
    };

    public ReloadMapForm() {
        super("Reloads all MapData.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<EditorContextData, ?> getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_EDITOR_CONTEXT;
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public String execute(Context context, Object[] arguments, EditorContextData data) {
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
            return ">red{" + StringUtils.escapify(e.getMessage()) + "}";
        }

        return "Reloaded all maps.";
    }
}
