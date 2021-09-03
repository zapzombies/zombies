package io.github.zap.zombies.command.game;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.game.ZombiesArena;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class GotoRoundForm extends CommandForm<Pair<Player, Arena<?>>> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("gotoRound"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[round-index]", Converters.INTEGER_CONVERTER)
    };

    private static final CommandValidator<Pair<Player, Arena<?>>, Player> validator =
            new CommandValidator<>((context, objects, player) -> {
                Iterator<? extends Arena<?>> arenaIterator = ArenaApi.getInstance().arenaIterator();
                while(arenaIterator.hasNext()) {
                    Arena<?> next = arenaIterator.next();
                    if(next.hasPlayer(player.getUniqueId())) {
                        if(next instanceof ZombiesArena zombiesArena) {
                            int roundIndex = (int)objects[1];
                            if(roundIndex <= zombiesArena.getMap().getRounds().size() && roundIndex > 0) {
                                if(zombiesArena.getMap().getCurrentRoundProperty().getValue(zombiesArena) == roundIndex - 1) {
                                    return ValidationResult.of(false, "It is already round " + roundIndex + "!", null);
                                }

                                return ValidationResult.of(true, null, Pair.of(player, next));
                            }

                            return ValidationResult.of(false, "Rounds index out of bounds for this map!",
                                    null);
                        }

                        return ValidationResult.of(false, "You cannot skip rounds in this arena!", null);
                    }
                }

                return ValidationResult.of(false, "You must be in a game to use that command!", null);
    }, Validators.PLAYER_EXECUTOR);

    public GotoRoundForm() {
        super("Skips to the desired round.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Pair<Player, Arena<?>>, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] objects, Pair<Player, Arena<?>> data) {
        int roundIndex = (int)objects[1] - 1;
        ZombiesArena zombiesArena = (ZombiesArena) data.getRight();
        zombiesArena.doRound(roundIndex);
        return "Going to round " + (roundIndex + 1);
    }
}
