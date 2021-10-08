package io.github.zap.zombies.command.game;

import com.google.common.collect.ImmutableList;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.util.Validators;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class QuitGameForm extends CommandForm<Pair<Player, Arena<?>>> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("quit", Component.text("quit"))
    };

    private static final CommandValidator<Pair<Player, Arena<?>>, Player> validator =
            new CommandValidator<>((context, objects, player) -> {
            Iterator<? extends Arena<?>> arenaIterator = ArenaApi.getInstance().arenaIterator();
        while(arenaIterator.hasNext()) {
            Arena<?> next = arenaIterator.next();
            if(next.hasPlayer(player.getUniqueId())) {
                return ValidationResult.of(true, null, Pair.of(player, next));
            }
        }

        return ValidationResult.of(false, Component.text("You must be in a game to use that command!"),
                null);
    }, Validators.PLAYER_EXECUTOR);

    public QuitGameForm(@NotNull RegularCommand command) {
        super(command, Component.text("Quits a game."), Permissions.NONE, parameters);
    }

    @Override
    public CommandValidator<Pair<Player, Arena<?>>, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public @NotNull Component execute(Context context, Object[] objects, Pair<Player, Arena<?>> data) {
        data.getRight().handleLeave(ImmutableList.of(data.getLeft()));
        return Component.text("Leaving arena...");
    }
}
