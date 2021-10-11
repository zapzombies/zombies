package io.github.zap.zombies.command;

import io.github.zap.regularcommands.commands.BasicPageBuilder;
import io.github.zap.regularcommands.commands.CommandManager;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.game.GiveGoldForm;
import io.github.zap.zombies.command.game.GotoRoundForm;
import io.github.zap.zombies.command.game.QuitGameForm;
import org.jetbrains.annotations.NotNull;

/**
 * General command used by this plugin.
 */
public class ZombiesCommand extends RegularCommand {
    public ZombiesCommand(@NotNull CommandManager manager) {
        super(manager, "zap", new BasicPageBuilder());
        addForm(new JoinZombiesGameForm(this));
        addForm(new RejoinZombiesGameForm(this));
        addForm(new QuitGameForm(this));
        addForm(new GotoRoundForm(this));
        addForm(new GiveGoldForm(this));
    }
}
