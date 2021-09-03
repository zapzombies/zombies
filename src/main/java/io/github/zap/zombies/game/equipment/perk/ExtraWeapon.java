package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.hotbar.HotbarProfile;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.ExtraWeaponData;
import io.github.zap.zombies.game.data.equipment.perk.ExtraWeaponLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * Allows players to have extra weapon slots
 */
public class ExtraWeapon extends MarkerPerk<ExtraWeaponData, ExtraWeaponLevel> {

    private int previousLevel = -1; // local variable since we cannot reliably get the previous level

    public ExtraWeapon(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                       @NotNull ExtraWeaponData perkData) {
        super(arena, player, slot, perkData);
        activate(); // activate again because previousLevel was set to 0 before initialization
    }

    private void setLevel(int level) {
        HotbarManager hotbarManager = getZombiesPlayer().getHotbarManager();
        HotbarProfile defaultProfile = hotbarManager.getProfiles().get(HotbarManager.DEFAULT_PROFILE_NAME);

        while (previousLevel < level) {
            Map<String, Set<Integer>> removeSlots = getEquipmentData().getLevels().get(++previousLevel).getNewSlots();

            for (Map.Entry<String, Set<Integer>> slotGroupPair : removeSlots.entrySet()) {
                HotbarObjectGroup group = hotbarManager.getHotbarObjectGroup(slotGroupPair.getKey());

                if (group != null) {
                    for (Integer slot : slotGroupPair.getValue()) {
                        defaultProfile.swapSlotOwnership(slot, group);
                    }
                }
            }
        }

        while (previousLevel > level) {
            for (Set<Integer> slots : getEquipmentData().getLevels().get(previousLevel--).getNewSlots().values()) {
                for (Integer slot : slots) {
                    defaultProfile.removeHotbarObject(slot, false);
                }
            }
        }
    }

    @Override
    public void activate() {
        setLevel(getLevel());
    }

    @Override
    public void deactivate() {
        setLevel(-1);
    }

}
