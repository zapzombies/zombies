package io.github.zap.zombies.game.equipment;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A piece of equipment in the hotbar
 * @param <D> The type of equipment data the equipment uses
 * @param <L> The type of level the equipment uses
 */
@Getter
public class Equipment<D extends EquipmentData<L>, L> extends HotbarObject {

    private final ZombiesArena arena;

    private final ZombiesPlayer zombiesPlayer;

    private final LocalizationManager localizationManager;

    private final D equipmentData;

    public Equipment(ZombiesArena arena, ZombiesPlayer player, int slot, D equipmentData) {
        super(player.getPlayer(), slot);

        if (player.getPlayer() == null) {
            throw new IllegalArgumentException("Attempted to create an equipment for offline player "
                    + player.getOfflinePlayer().getName() + " !");
        }

        this.arena = arena;
        this.zombiesPlayer = player;
        this.localizationManager = Zombies.getInstance().getLocalizationManager();
        this.equipmentData = equipmentData;
        setRepresentingItemStack(equipmentData.createItemStack(player.getPlayer(), 0));
    }

    /**
     * Gets the current level of the equipment
     * @return The current level of the equipment
     */
    public @NotNull L getCurrentLevel() {
        return equipmentData.getLevels().get(0);
    }


}
