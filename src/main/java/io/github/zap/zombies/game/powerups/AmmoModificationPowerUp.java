package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.GunData;
import io.github.zap.zombies.game.data.equipment.gun.GunLevel;
import io.github.zap.zombies.game.data.powerups.ModifierMode;
import io.github.zap.zombies.game.data.powerups.ModifierModeModificationPowerUpData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.util.MathUtils;

/**
 * This power up apply a function f(x) = x * multiplier + amount to all players ammo
 */
@PowerUpType(name = "Ammo-Modification")
// Apply multiplier before amount (addition), allow negative val
public class AmmoModificationPowerUp extends PowerUp {

    public AmmoModificationPowerUp(ModifierModeModificationPowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public AmmoModificationPowerUp(ModifierModeModificationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    @Override
    public void activate() {
        var cData = (ModifierModeModificationPowerUpData) getData();
        getArena().getPlayerMap().forEach((l,r) -> {
            var gunGroup = r.getHotbarManager()
                    .getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());
            if (gunGroup != null) {
                gunGroup.getHotbarObjectMap().forEach((slot, eq) -> {
                    if (eq instanceof Gun<? extends GunData<?>, ? extends GunLevel> gun) {
                        var reference = cData.getModifierMode() == ModifierMode.ABSOLUTE ? gun.getCurrentLevel().getAmmo() : gun.getCurrentAmmo();
                        gun.setAmmo((int) MathUtils.normalizeMultiplier(reference * cData.getMultiplier() + cData.getAmount(), reference));
                        gun.setClipAmmo(MathUtils.clamp(gun.getCurrentAmmo(), 0, gun.getCurrentLevel().getClipAmmo()));
                        gun.resetStates();
                    }
                });
            }
        });

        deactivate();
    }
}
