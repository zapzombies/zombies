package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.melee.AOEMeleeData;
import io.github.zap.zombies.game.data.equipment.melee.AOEMeleeLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Melee weapon which deals damage with an Area-of-Effect range
 */
public class AOEMeleeWeapon extends MeleeWeapon<AOEMeleeData, AOEMeleeLevel> {

    @RequiredArgsConstructor
    private class AOEMeleeDamageAttempt extends MeleeDamageAttempt {

        private final Mob mainMob;

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return mainMob.getUniqueId().equals(target.getUniqueId()) && super.ignoresArmor(damager, target);
        }

    }

    public AOEMeleeWeapon(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot,
                          AOEMeleeData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void attack(Mob mob) {
        ZombiesArena zombiesArena = getArena();
        ZombiesArena.DamageHandler damageHandler = zombiesArena.getDamageHandler();

        World world = mob.getWorld();
        Collection<Mob> aoeMobs
                = world.getNearbyEntitiesByType(Mob.class, mob.getLocation(), getCurrentLevel().getRange());

        DamageAttempt damageAttempt = new AOEMeleeDamageAttempt(mob);
        for (Mob otherMob : aoeMobs) {
            damageHandler.damageEntity(getZombiesPlayer(), damageAttempt, otherMob);
        }
    }

}
