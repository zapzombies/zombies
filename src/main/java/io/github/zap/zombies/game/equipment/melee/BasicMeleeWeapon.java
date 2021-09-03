package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.melee.BasicMeleeData;
import io.github.zap.zombies.game.data.equipment.melee.BasicMeleeLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.entity.Mob;

/**
 * A basic melee weapon implementation
 */
public class BasicMeleeWeapon extends MeleeWeapon<BasicMeleeData, BasicMeleeLevel> {

    public BasicMeleeWeapon(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot,
                            BasicMeleeData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void attack(Mob mob) {
        getArena().getDamageHandler().damageEntity(getZombiesPlayer(), new MeleeDamageAttempt(), mob);
    }
}
