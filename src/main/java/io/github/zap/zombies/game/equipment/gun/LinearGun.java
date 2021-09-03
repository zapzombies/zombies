package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.equipment.gun.logic.LinearBeam;
import io.github.zap.zombies.game.player.ZombiesPlayer;

/**
 * Represents a gun which shoots a line of particles and damages guns within a line
 */
public class LinearGun extends Gun<LinearGunData, LinearGunLevel> {

    public LinearGun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, LinearGunData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void shoot() {
        LinearGunData linearGunData = getEquipmentData();
        LinearGunLevel currentLevel = getCurrentLevel();

        new LinearBeam(
                getArena().getMap(),
                getZombiesPlayer(),
                getPlayer().getEyeLocation(),
                currentLevel,
                linearGunData.getParticle(),
                linearGunData.getParticleDataWrapper()
        ).send();
    }
}
