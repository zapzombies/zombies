package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.FastReviveData;
import io.github.zap.zombies.game.data.equipment.perk.FastReviveLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Revives player at a faster speed
 */
public class FastRevive extends MarkerPerk<FastReviveData, FastReviveLevel> {

    @Getter
    private int reducedReviveTime;

    public FastRevive(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                      @NotNull FastReviveData perkData) {
        super(arena, player, slot, perkData);
    }

    @Override
    public void activate() {
        reducedReviveTime = getCurrentLevel().getReducedReviveTime();
    }

    @Override
    public void deactivate() {
        reducedReviveTime = 0;
    }

}
