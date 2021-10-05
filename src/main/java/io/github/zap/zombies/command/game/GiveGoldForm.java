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
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class GiveGoldForm extends CommandForm<ZombiesPlayer> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("giveGold"),
            new Parameter(Regexes.INTEGER, "[gold-amount]", Converters.INTEGER_CONVERTER)
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

                        return ValidationResult.of(false, "You cannot give yourself gold in this arena!", null);
                    }
                }

                return ValidationResult.of(false, "You must be in a game to use that command!", null);
    }, Validators.PLAYER_EXECUTOR);

    public GiveGoldForm() {
        super("Gives the player some hecking gold.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<ZombiesPlayer, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] objects, ZombiesPlayer player) {
        player.addCoins((int)objects[1]);
        return "Gave gold.";
    }
}
