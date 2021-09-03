package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.QuickFireData;
import io.github.zap.zombies.game.data.equipment.perk.QuickFireLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Makes guns shoot faster
 */
public class QuickFire extends MarkerPerk<QuickFireData, QuickFireLevel> {

    private static final String MODIFIER_NAME = "quick_fire";

    public QuickFire(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                     @NotNull QuickFireData perkData) {
        super(arena, player, slot, perkData);
    }

    @Override
    public void activate() {
        getZombiesPlayer().getFireRateMultiplier().registerModifier(MODIFIER_NAME,
                (rate) -> ((rate == null) ? 1D : rate) * getCurrentLevel().getMultiplier());
    }

    @Override
    public void deactivate() {
        getZombiesPlayer().getFireRateMultiplier().removeModifier(MODIFIER_NAME);
    }

}
