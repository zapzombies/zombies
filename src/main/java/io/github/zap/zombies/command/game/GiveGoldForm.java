package io.github.zap.zombies.command.game;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Converters;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.util.Validators;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class GiveGoldForm extends CommandForm<ZombiesPlayer> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("giveGold", Component.text("giveGold")),
            new Parameter(Regexes.INTEGER, Component.text("[gold-amount]"), Converters.INTEGER_CONVERTER)
    };

    private static final CommandValidator<ZombiesPlayer, Player> validator =
            new CommandValidator<>((context, objects, player) -> {
                Iterator<? extends Arena<?>> arenaIterator = ArenaApi.getInstance().arenaIterator();
                while(arenaIterator.hasNext()) {
                    Arena<?> next = arenaIterator.next();
                    if(next.hasPlayer(player.getUniqueId())) {
                        if(next instanceof ZombiesArena zombiesArena) {
                            ZombiesPlayer zombiesPlayer = zombiesArena.getPlayerMap().get(player.getUniqueId());

                            if(zombiesPlayer != null) {
                                return ValidationResult.of(true, null, zombiesPlayer);
                            }
                        }

                        return ValidationResult.of(false, Component.text("You cannot give yourself gold " +
                                "in this arena!"), null);
                    }
                }

                return ValidationResult.of(false, Component.text("You must be in a game to use that " +
                        "command!"), null);
    }, Validators.PLAYER_EXECUTOR);

    public GiveGoldForm(@NotNull RegularCommand command) {
        super(command, Component.text("Gives the player some hecking gold."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<ZombiesPlayer, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public Component execute(Context context, Object[] objects, ZombiesPlayer player) {
        player.addCoins((int)objects[1]);
        return Component.text("Gave gold.");
    }
}