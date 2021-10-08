package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.util.Validators;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveWandForm extends CommandForm<Player> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map", Component.text("map")),
            new Parameter("wand", Component.text("wand"))
    };

    public GiveWandForm(@NotNull RegularCommand command) {
        super(command, Component.text("Gives the player the mapeditor wand."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Player, ?> getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @Override
    public Component execute(Context context, Object[] arguments, Player player) {
        player.getInventory().addItem( Zombies.getInstance().getContextManager().getEditorItem());
        return Component.text("Gave editor wand.");
    }
}
