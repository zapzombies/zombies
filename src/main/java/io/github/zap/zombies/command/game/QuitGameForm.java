package io.github.zap.zombies.command.game;

import com.google.common.collect.ImmutableList;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class QuitGameForm extends CommandForm<Pair<Player, Arena<?>>> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("quit")
    };

    private static final CommandValidator<Pair<Player, Arena<?>>, Player> validator = new CommandValidator<>((context, objects, player) -> {
            Iterator<? extends Arena<?>> arenaIterator = ArenaApi.getInstance().arenaIterator();
        while(arenaIterator.hasNext()) {
            Arena<?> next = arenaIterator.next();
            if(next.hasPlayer(player.getUniqueId())) {
                return ValidationResult.of(true, null, Pair.of(player, next));
            }
        }

        return ValidationResult.of(false, "You must be in a game to use that command!", null);
    }, Validators.PLAYER_EXECUTOR);

    public QuitGameForm() {
        super("Quits a game.", Permissions.NONE, parameters);
    }

    @Override
    public CommandValidator<Pair<Player, Arena<?>>, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] objects, Pair<Player, Arena<?>> data) {
        data.getRight().handleLeave(ImmutableList.of(data.getLeft()));
        return "Leaving arena...";
    }
}
