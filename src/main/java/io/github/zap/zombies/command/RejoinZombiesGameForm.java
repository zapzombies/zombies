package io.github.zap.zombies.command;

import com.google.common.collect.Lists;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.SimpleJoinable;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RejoinZombiesGameForm extends CommandForm<Joinable> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("rejoin"),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[arena-name]"),
            new Parameter("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}",
                    "[map-uuid]")
    };

    private static final CommandValidator<Joinable, ?> validator = new CommandValidator<>((context, arguments,
                                                                                           previousData) -> {
        String managerName = (String) arguments[1];
        String mapUUIDString = (String) arguments[2];

        ArenaManager<?> arenaManager = ArenaApi.getInstance().getArenaManager(managerName);

        if (arenaManager == null) {
            return ValidationResult.of(false, String.format("An ArenaManager named '%s' does not exist.",
                    managerName), null);
        }

        try {
            UUID mapUUID = UUID.fromString(mapUUIDString);

            if (!arenaManager.getArenas().containsKey(mapUUID)) {
                return ValidationResult.of(false, String.format("There is no map with UUID '%s' for " +
                        "ArenaManager '%s'", mapUUIDString, managerName), null);
            }
        } catch (IllegalArgumentException e) {
            return ValidationResult.of(false, String.format("The given UUID string '%s' is invalid.",
                    mapUUIDString), null);
        }

        return ValidationResult.of(true, null, new SimpleJoinable(Lists.newArrayList(previousData)));
    }, Validators.PLAYER_EXECUTOR);

    public RejoinZombiesGameForm() {
        super("Rejoins a Zombies game.", Permissions.OPERATOR, parameters);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator<Joinable, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Joinable data) {
        Player player = (Player) context.getSender();
        ArenaApi api = Zombies.getInstance().getArenaApi();

        JoinInformation testInformation = new JoinInformation(data, (String) arguments[1],
                null, UUID.fromString((String) arguments[2]), null);

        api.handleJoin(testInformation, (pair) -> {
            if (!pair.getLeft()) {
                player.sendMessage(pair.getRight());
            }
        });

        return ">green{Attempting to rejoin a game...}";
    }
}
