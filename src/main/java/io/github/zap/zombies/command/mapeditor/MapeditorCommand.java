package io.github.zap.zombies.command.mapeditor;

import io.github.zap.regularcommands.commands.BasicPageBuilder;
import io.github.zap.regularcommands.commands.CommandManager;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.mapeditor.form.*;
import org.jetbrains.annotations.NotNull;

public class MapeditorCommand extends RegularCommand {
    public MapeditorCommand(@NotNull CommandManager manager) {
        super(manager,"mapeditor", new BasicPageBuilder());
        addForm(new EditMapForm(this));
        addForm(new GiveWandForm(this));
        addForm(new DeleteMapForm(this));
        addForm(new ListMapForm(this));
        addForm(new NewMapForm(this));
        addForm(new NewRoomForm(this));
        addForm(new NewWindowForm(this));
        addForm(new WindowBoundsForm(this));
        addForm(new ExitEditorForm(this));
        addForm(new NewSpawnpointForm(this));
        addForm(new ReloadMapForm(this));
        addForm(new NewDoorForm(this));
        addForm(new NewDoorSideForm(this));
        addForm(new NewShopForm(this));
        addForm(new NewSpawnruleForm(this));
        addForm(new NewRoundForm(this));
        addForm(new NewWaveForm(this));
        addForm(new NewSpawnEntryForm(this));
        addForm(new SaveMapForm(this));
    }
}
