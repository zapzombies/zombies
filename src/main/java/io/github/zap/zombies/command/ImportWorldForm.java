package io.github.zap.zombies.command;

import com.grinderwolf.swm.api.exceptions.InvalidWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.exceptions.WorldLoadedException;
import com.grinderwolf.swm.api.exceptions.WorldTooBigException;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.StringUtils;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class ImportWorldForm extends CommandForm<String> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("import"),
            new Parameter("world"),
            new Parameter("^([a-zA-Z0-9_]+)$", "[world_name]")
    };

    private static final CommandValidator<String, ?> validator = new CommandValidator<>((context, arguments, previousData) -> {
        String worldName = (String)arguments[2];

        if(Zombies.getInstance().getServer().getWorlds().stream().map(World::getName)
                .collect(Collectors.toList()).contains(worldName)) {
            return ValidationResult.of(false, "That world is loaded and thus cannot be imported.", null);
        }

        return ValidationResult.of(true, null, worldName);
    }, Validators.PLAYER_EXECUTOR);

    public ImportWorldForm() {
        super("Imports a world to the SWM format.", Permissions.OPERATOR, parameters);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator<String, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, String worldName) {
        Zombies zombies = Zombies.getInstance();

        try {
            zombies.getSWM().importWorld(new File(worldName), worldName, zombies.getSlimeLoader());
        } catch (WorldTooBigException | WorldLoadedException | WorldAlreadyExistsException | IOException |
                InvalidWorldException e) {
            return String.format("Failed to import world. Reason: >red{%s}", StringUtils.escapify(e.getMessage()));
        }

        return ">green{World imported.}";
    }
}
