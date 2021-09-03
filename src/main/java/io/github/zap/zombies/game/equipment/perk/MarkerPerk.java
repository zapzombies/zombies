package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.PerkData;
import io.github.zap.zombies.game.data.equipment.perk.PerkLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a perk hotbar equipment that does not involve a recurring event.
 */
public abstract class MarkerPerk<D extends PerkData<L>, L extends PerkLevel>
        extends Perk<D, L, Event<EmptyEventArgs>, EmptyEventArgs> {

    public MarkerPerk(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                      @NotNull D perkData) {
        super(arena, player, slot, perkData, null);
    }

    @Override
    public void execute(@Nullable EmptyEventArgs args) { }

}
