package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunData;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunLevel;
import io.github.zap.zombies.game.equipment.gun.logic.ZapperBeam;
import io.github.zap.zombies.game.player.ZombiesPlayer;

/**
 * Represents a gun that zaps entities
 */
public class ZapperGun extends Gun<ZapperGunData, ZapperGunLevel> {

    public ZapperGun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, ZapperGunData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void shoot() {
        ZapperGunData zapperGunData = getEquipmentData();
        ZapperGunLevel currentLevel = zapperGunData.getLevels().get(getLevel());

        new ZapperBeam(
                getArena().getMap(),
                getZombiesPlayer(),
                getPlayer().getEyeLocation(),
                currentLevel,
                zapperGunData.getParticle(),
                zapperGunData.getParticleDataWrapper()
        ).send();
    }

}
