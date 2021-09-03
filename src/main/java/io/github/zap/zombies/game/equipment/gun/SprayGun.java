package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.SprayGunData;
import io.github.zap.zombies.game.data.equipment.gun.SprayGunLevel;
import io.github.zap.zombies.game.equipment.gun.logic.LinearBeam;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Represents a gun that shoots a spray of bullets, similarly to linear guns
 */
public class SprayGun extends Gun<SprayGunData, SprayGunLevel> {

    private static final Random RANDOM = new Random();

    public SprayGun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, SprayGunData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void shoot() {
        Location eyeLocation = getPlayer().getEyeLocation();

        SprayGunData sprayGunData = getEquipmentData();
        SprayGunLevel currentLevel = sprayGunData.getLevels().get(getLevel());

        for (int i = 0; i < currentLevel.getPellets(); i++) {
            float angle = currentLevel.getConeAngle();
            float dYaw = angle * (2 * RANDOM.nextFloat() - 1F), dPitch = angle * (2 * RANDOM.nextFloat() - 1F);

            Location eyeLocationCopy = eyeLocation.clone();
            eyeLocationCopy.setYaw(eyeLocationCopy.getYaw() + dYaw);
            eyeLocationCopy.setPitch(eyeLocationCopy.getPitch() + dPitch);

            new LinearBeam(
                    getArena().getMap(),
                    getZombiesPlayer(),
                    eyeLocationCopy,
                    currentLevel,
                    sprayGunData.getParticle(),
                    sprayGunData.getParticleDataWrapper()
            ) {
                @Override
                protected void damageEntity(RayTraceResult rayTraceResult) {
                    Mob mob = (Mob) rayTraceResult.getHitEntity();

                    if (mob != null) {
                        ZombiesArena arena = getZombiesPlayer().getArena();
                        arena.getDamageHandler().damageEntity(getZombiesPlayer(),
                                new BeamDamageAttempt(determineIfHeadshot(rayTraceResult, mob)) {
                                    @Override
                                    public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
                                        return super.knockbackFactor(damager, target) / currentLevel.getPellets();
                                    }
                                }, mob);
                    }
                }
            }.send();
        }
    }

}
