package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.mapeditor.form.*;

public class MapeditorCommand extends RegularCommand {
    public MapeditorCommand() {
        super("mapeditor");
        addForm(new EditMapForm());
        addForm(new GiveWandForm());
        addForm(new DeleteMapForm());
        addForm(new ListMapForm());
        addForm(new NewMapForm());
        addForm(new NewRoomForm());
        addForm(new NewWindowForm());
        addForm(new WindowBoundsForm());
        addForm(new ExitEditorForm());
        addForm(new NewSpawnpointForm());
        addForm(new ReloadMapForm());
        addForm(new NewDoorForm());
        addForm(new NewDoorSideForm());
        addForm(new NewShopForm());
        addForm(new NewSpawnruleForm());
        addForm(new NewRoundForm());
        addForm(new NewWaveForm());
        addForm(new NewSpawnEntryForm());
        addForm(new SaveMapForm());
    }
}
