package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;

/**
 * Task which refills all gun ammo in a player team
 */
public class AmmoSupply extends TeamMachineTask {

    public AmmoSupply() {
        super(TeamMachineTaskType.AMMO_SUPPLY.name());
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(teamMachine, zombiesArena, zombiesPlayer)) {
            for (ZombiesPlayer otherZombiesPlayer : zombiesArena.getPlayerMap().values()) {
                GunObjectGroup gunObjectGroup = (GunObjectGroup)
                        otherZombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());

                if (gunObjectGroup != null) {
                    for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof Gun<?, ?> gun) {
                            gun.refill();
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public int getCostForTeamMachine(TeamMachine teamMachine) {
        return getInitialCost();
    }
}
