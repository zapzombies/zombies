package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a team machine
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class TeamMachineData extends BlockShopData {

    private List<TeamMachineTask> teamMachineTasks = new ArrayList<>();

    private TeamMachineData() {
        super(ShopType.TEAM_MACHINE.name(), true, null, null);
    }

    public TeamMachineData(@NotNull Vector blockLocation, @NotNull Vector hologramLocation) {
        super(ShopType.TEAM_MACHINE.name(), true, blockLocation, hologramLocation);
    }

}
