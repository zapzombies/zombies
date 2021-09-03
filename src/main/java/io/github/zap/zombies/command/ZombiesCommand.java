package io.github.zap.zombies.command;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.game.GotoRoundForm;
import io.github.zap.zombies.command.game.QuitGameForm;

/**
 * General command used by this plugin.
 */
public class ZombiesCommand extends RegularCommand {
    public ZombiesCommand() {
        super("zap");
        addForm(new MapLoaderProfilerForm());
        addForm(new ImportWorldForm());
        addForm(new JoinZombiesGameForm());
        addForm(new RejoinZombiesGameForm());
        addForm(new QuitGameForm());
        addForm(new GotoRoundForm());
    }
}
