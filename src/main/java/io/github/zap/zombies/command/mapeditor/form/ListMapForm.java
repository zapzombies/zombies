package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.util.Validators;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ListMapForm extends CommandForm<Player> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("list")
    };

    public ListMapForm(@NotNull RegularCommand command) {
        super(command, Component.text("Lists all maps that currently exist."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Player, ?> getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @Override
    public Component execute(Context context, Object[] arguments, Player player) {
        for(MapData map : Zombies.getInstance().getArenaManager().getMaps()) {
            player.sendMessage(map.getName());
        }

        return null;
    }
}
