package io.github.zap.zombies.game.damage;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.data.powerups.DamageModificationPowerUpData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.powerups.DamageModificationPowerUp;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public class BasicDamageHandler implements DamageHandler {

    private final Plugin plugin;

    private final Collection<PowerUp> powerUps;

    private final List<KnockbackModifier> knockbackModifiers;

    private final Set<Mob> mobs;

    public BasicDamageHandler(@NotNull Plugin plugin, @NotNull Collection<PowerUp> powerUps,
                              @NotNull List<KnockbackModifier> knockbackModifiers, @NotNull Set<Mob> mobs) {
        this.plugin = plugin;
        this.powerUps = powerUps;
        this.knockbackModifiers = knockbackModifiers;
        this.mobs = mobs;
    }

    @Override
    public void damageEntity(@NotNull Damager damager, @NotNull DamageAttempt with, @NotNull Mob target) {
        if (mobs.contains(target) && !target.isDead()) {
            target.playEffect(EntityEffect.HURT);

            double mobKbFactor = 1.0D;
            for (KnockbackModifier knockbackModifier : knockbackModifiers) {
                mobKbFactor = knockbackModifier.modify(target, mobKbFactor);
            }

            Player player = null;
            if (damager instanceof ZombiesPlayer zp) {
                player = zp.getPlayer();
            }

            double deltaHealth = inflictDamage(player, target, with.damageAmount(damager, target),
                    with.ignoresArmor(damager, target));
            Vector resultingVelocity = target.getVelocity().add(with.directionVector(damager, target)
                    .multiply(with.knockbackFactor(damager, target)).multiply(mobKbFactor));

            try {
                target.setVelocity(resultingVelocity);
            }
            catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Attempted to set velocity for entity "
                        + target.getUniqueId() + " to a vector " + "with a non-finite value " + resultingVelocity);
            }

            damager.onDealsDamage(with, target, deltaHealth);
        }
    }

    private double inflictDamage(Entity damager, Damageable mob, double damage, boolean ignoreArmor) {
        boolean instaKill = false;

        for (PowerUp powerup : powerUps) {
            if(powerup instanceof DamageModificationPowerUp && powerup.getState() == PowerUpState.ACTIVATED) {
                var data = (DamageModificationPowerUpData) powerup.getData();
                if(data.isInstaKill()) {
                    instaKill = true;
                    break;
                }

                damage = damage * data.getMultiplier() + data.getAdditionalDamage();
            }
        }

        double before = mob.getHealth();

        Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(mob.getUniqueId());
        boolean resistInstakill = false;
        if (activeMob.isPresent()) {
            resistInstakill = activeMob.get().getType().getConfig().getBoolean("ResistInstakill", false);
        }

        if (instaKill && !resistInstakill) {
            double health = mob.getHealth();
            mob.setHealth(0);

            if (damager != null) {
                plugin.getServer().getPluginManager().callEvent(new EntityDamageByEntityEvent(damager, mob,
                        EntityDamageEvent.DamageCause.CUSTOM, health));
            }
        } else if (ignoreArmor) {
            mob.setHealth(Math.max(mob.getHealth() - damage, 0D));

            if (damager != null) {
                plugin.getServer().getPluginManager().callEvent(new EntityDamageByEntityEvent(damager, mob,
                        EntityDamageEvent.DamageCause.CUSTOM, damage));
            }
        } else {
            mob.damage(damage, null);
        }

        mob.playEffect(EntityEffect.HURT);
        return before - mob.getHealth();
    }

}
