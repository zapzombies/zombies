package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.FlamingBulletsData;
import io.github.zap.zombies.game.data.equipment.perk.FlamingBulletsLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Sets zombies on fire
 */
public class FlamingBullets extends MarkerPerk<FlamingBulletsData, FlamingBulletsLevel> {

    @Getter
    private int duration;

    public FlamingBullets(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                          @NotNull FlamingBulletsData perkData) {
        super(arena, player, slot, perkData);
    }

    @Override
    public void activate() {
        duration = getCurrentLevel().getDuration();
    }

    @Override
    public void deactivate() {
        duration = 0;
    }

}
