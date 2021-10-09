package io.github.zap.zombies.command;

import com.google.common.collect.Lists;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.SimpleJoinable;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.util.Validators;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RejoinZombiesGameForm extends CommandForm<Joinable> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("rejoin", Component.text("rejoin")),
            new Parameter("^([a-zA-Z0-9_ ]+)$", Component.text("[arena-name]"), false),
            new Parameter("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}",
                    Component.text("[map-uuid]"), false)
    };

    private static final CommandValidator<Joinable, ?> validator = new CommandValidator<>((context, arguments,
                                                                                           previousData) -> {
        String managerName = (String) arguments[1];
        String mapUUIDString = (String) arguments[2];

        ArenaManager<?> arenaManager = ArenaApi.getInstance().getArenaManager(managerName);

        if (arenaManager == null) {
            return ValidationResult.of(false, Component.text("An ArenaManager named " + managerName +
                    " does not exist."), null);
        }

        try {
            UUID mapUUID = UUID.fromString(mapUUIDString);

            if (!arenaManager.getArenas().containsKey(mapUUID)) {
                return ValidationResult.of(false, Component.text("There is no map with UUID " + mapUUIDString +
                        "for ArenaManager " + arenaManager.getGameName()), null);
            }
        } catch (IllegalArgumentException e) {
            return ValidationResult.of(false, Component.text("The given UUID string " + mapUUIDString +
                    " is not valid"), null);
        }

        return ValidationResult.of(true, null, new SimpleJoinable(Lists.newArrayList(previousData)));
    }, Validators.PLAYER_EXECUTOR);

    public RejoinZombiesGameForm(@NotNull RegularCommand command) {
        super(command, Component.text("Rejoins a Zombies game."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Joinable, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public @NotNull Component execute(Context context, Object[] arguments, Joinable data) {
        Player player = (Player) context.getSender();
        ArenaApi api = Zombies.getInstance().getArenaApi();

        JoinInformation testInformation = new JoinInformation(data, (String) arguments[1],
                null, UUID.fromString((String) arguments[2]), null);

        api.handleJoin(testInformation, (pair) -> {
            if (!pair.getLeft()) {
                player.sendMessage(pair.getRight());
            }
        });

        return Component.text("Attempting to rejoin a game...");
    }
}
