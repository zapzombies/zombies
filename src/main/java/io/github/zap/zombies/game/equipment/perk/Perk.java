package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.PerkData;
import io.github.zap.zombies.game.data.equipment.perk.PerkLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a perk hotbar equipment
 * Perks can be event-based: their actions are performed when the provided event is fired. If
 * perks do not require any kind of action, the event can be set to null.
 * @param <D> The perk data type
 * @param <L> The perk level type
 * @param <E> The perk executor type
 * @param <T> The type of arguments the event passes to the perk executor
 */
public abstract class Perk<D extends PerkData<L>, L extends PerkLevel, E extends Event<T>, T>
        extends UpgradeableEquipment<D, L> {

    @Getter
    private final E actionTriggerEvent;

    public Perk(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                @NotNull D perkData, @Nullable E actionTriggerEvent) {
        super(arena, player, slot, perkData);

        this.actionTriggerEvent = actionTriggerEvent;

        if (actionTriggerEvent != null) {
            actionTriggerEvent.registerHandler(this::execute);
        }

        activate();
    }

    @Override
    public void upgrade() {
        super.upgrade();

        activate();
    }

    @Override
    public void downgrade() {
        super.downgrade();

        activate();
    }

    @Override
    public void remove() {
        super.remove();

        if (actionTriggerEvent != null) {
            actionTriggerEvent.removeHandler(this::execute);
        }

        deactivate();
    }

    /**
     * Applies the perk's effects. This is called every time the perk is upgraded or downgraded (if the downgrade
     * brings the perk down one level but is still not 0). It is also called when perks should be re-activated, such
     * as when the player rejoins the game.
     */
    public abstract void activate();

    /**
     * Removes the perk's effects. This is called when the player leaves the game, or when the perk's level has been
     * reduced to 0.
     */
    public abstract void deactivate();

    /**
     * Performs the action associated with the perk. This is called automatically.
     * @param args The args passed by the event
     */
    public abstract void execute(@Nullable T args);

}
