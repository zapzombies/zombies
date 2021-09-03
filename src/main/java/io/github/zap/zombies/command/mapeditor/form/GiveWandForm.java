package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

public class GiveWandForm extends CommandForm<Player> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("wand")
    };

    public GiveWandForm() {
        super("Gives the player the mapeditor wand.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Player, ?> getValidator(Context context, Object[] arguments) {
        return Validators.PLAYER_EXECUTOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Player player) {
        player.getInventory().addItem( Zombies.getInstance().getContextManager().getEditorItem());
        return "Gave editor wand.";
    }
}
