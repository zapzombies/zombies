package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayerState;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;

/**
 * Task which revives all knocked down players in an arena
 */
public class FullRevive extends TeamMachineTask {

    public FullRevive() {
        super(TeamMachineTaskType.FULL_REVIVE.name());
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(teamMachine, zombiesArena, zombiesPlayer)){
            for (ZombiesPlayer otherZombiesPlayer : zombiesArena.getPlayerMap().values()) {
                if (otherZombiesPlayer.isInGame()) {
                    if (otherZombiesPlayer.getState() == ZombiesPlayerState.KNOCKED) {
                        otherZombiesPlayer.revive();
                    } else if (otherZombiesPlayer.getState() == ZombiesPlayerState.DEAD) {
                        otherZombiesPlayer.respawn();
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
