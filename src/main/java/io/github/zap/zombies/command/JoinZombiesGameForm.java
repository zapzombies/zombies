package io.github.zap.zombies.command;

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
import io.github.zap.party.Party;
import io.github.zap.party.PartyPlusPlus;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Optional;

public class JoinZombiesGameForm extends CommandForm<Joinable> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("join"),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[arena-name]"),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[map-name]")
    };

    private static final CommandValidator<Joinable, ?> validator = new CommandValidator<>((context, arguments,
                                                                                           previousData) -> {
        String managerName = (String) arguments[1];
        String mapName = (String) arguments[2];

        ArenaManager<?> arenaManager = ArenaApi.getInstance().getArenaManager(managerName);

        if(arenaManager == null) {
            return ValidationResult.of(false, String.format("An ArenaManager named '%s' does not exist.",
                    managerName), null);
        }

        if (!arenaManager.hasMap(mapName)) {
            return ValidationResult.of(false, String.format("A map named '%s' does not exist for " +
                    "ArenaManager '%s'", mapName, managerName), null);
        }

        Joinable joinable = null;
        PartyPlusPlus partyPlusPlus = ArenaApi.getInstance().getPartyPlusPlus();
        if (partyPlusPlus != null) {
            Optional<Party> partyOptional = partyPlusPlus.getPartyTracker().getPartyForPlayer(previousData);
            if (partyOptional.isPresent()) {
                if (!partyOptional.get().isOwner(previousData)) {
                    return ValidationResult.of(false, "You are not the owner of the party!", null);
                }
                joinable = new SimpleJoinable(partyOptional.get().getOnlinePlayers());
            }
        }
        if (joinable == null) {
            joinable = new SimpleJoinable(Collections.singletonList(previousData));
        }

        return ValidationResult.of(true, null, joinable);
    }, Validators.PLAYER_EXECUTOR);

    public JoinZombiesGameForm() {
        super("Joins a Zombies game.", Permissions.OPERATOR, parameters);
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
        JoinInformation testInformation = new JoinInformation(data, (String)arguments[1], (String)arguments[2],
                null, null);

        api.handleJoin(testInformation, (pair) -> {
            if(!pair.getLeft()) {
                player.sendMessage(pair.getRight());
            }
        });

        return ">green{Attempting to join a game...}";
    }
}
