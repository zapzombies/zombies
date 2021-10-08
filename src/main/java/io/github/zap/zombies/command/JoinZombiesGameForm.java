package io.github.zap.zombies.command;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.SimpleJoinable;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.party.Party;
import io.github.zap.party.plugin.PartyPlugin;
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

import java.util.Collections;
import java.util.Optional;

public class JoinZombiesGameForm extends CommandForm<Joinable> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("join", Component.text("join")),
            new Parameter("^([a-zA-Z0-9_ ]+)$", Component.text("[arena-name]")),
            new Parameter("^([a-zA-Z0-9_ ]+)$", Component.text("[map-name]"))
    };

    private static final CommandValidator<Joinable, ?> validator = new CommandValidator<>((context, arguments,
                                                                                           previousData) -> {
        String managerName = (String) arguments[1];
        String mapName = (String) arguments[2];

        ArenaManager<?> arenaManager = ArenaApi.getInstance().getArenaManager(managerName);

        if(arenaManager == null) {
            return ValidationResult.of(false, Component.text("An ArenaManager named " + managerName +
                    " does not exist!"), null);
        }

        if (!arenaManager.hasMap(mapName)) {
            return ValidationResult.of(false, Component.text("A map named " + mapName +
                    " does not exist for ArenaManager " + managerName), null);
        }

        Joinable joinable = null;

        PartyPlugin partyPlusPlus = ArenaApi.getInstance().getPartyPlusPlus();
        if (partyPlusPlus != null) {
            Optional<Party> partyOptional = partyPlusPlus.getPartyTracker().getPartyForPlayer(previousData);
            if (partyOptional.isPresent()) {
                if (!partyOptional.get().isOwner(previousData)) {
                    return ValidationResult.of(false, Component.text("You are not the owner of the party!"), null);
                }
                joinable = new SimpleJoinable(partyOptional.get().getOnlinePlayers());
            }
        }
        if (joinable == null) {
            joinable = new SimpleJoinable(Collections.singletonList(previousData));
        }

        return ValidationResult.of(true, null, joinable);
    }, Validators.PLAYER_EXECUTOR);

    public JoinZombiesGameForm(@NotNull RegularCommand command) {
        super(command, Component.text("Joins a Zombies game."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Joinable, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public @NotNull Component execute(Context context, Object[] arguments, Joinable data) {
        Player player = (Player) context.getSender();
        ArenaApi api = Zombies.getInstance().getArenaApi();
        JoinInformation testInformation = new JoinInformation(data, (String)arguments[1], (String)arguments[2],
                null, null);

        api.handleJoin(testInformation, (pair) -> {
            if(!pair.getLeft()) {
                player.sendMessage(pair.getRight());
            }
        });

        return Component.text("Attempting to join a game...");
    }
}
