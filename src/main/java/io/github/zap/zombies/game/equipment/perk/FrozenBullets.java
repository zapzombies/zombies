package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.FrozenBulletsData;
import io.github.zap.zombies.game.data.equipment.perk.FrozenBulletsLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Reduces the speed of zombies
 */
public class FrozenBullets extends MarkerPerk<FrozenBulletsData, FrozenBulletsLevel> {

    @Getter
    private double reducedSpeed;

    @Getter
    private int duration;

    public FrozenBullets(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                         @NotNull FrozenBulletsData perkData) {
        super(arena, player, slot, perkData);
    }

    @Override
    public void activate() {
        reducedSpeed = getCurrentLevel().getReducedSpeed();
        duration = getCurrentLevel().getDuration();
    }

    @Override
    public void deactivate() {
        reducedSpeed = 0.0D;
        duration = 0;
    }

}
