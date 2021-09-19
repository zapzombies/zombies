package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@MythicMechanic(
        name = "zombiesShootSkull",
        description = "Shoots a Wither skull at the player, which applies a wither effect with an adjustable duration."
)
public class ShootSkullMechanic extends ZombiesPlayerSkill implements Listener {
    private static final String EFFECT_DURATION = "skill.shootskull.effect_duration";
    private static final String EFFECT_AMPLIFIER = "skill.shootskull.effect_amplifier";

    private static class Handler implements Listener {
        private Handler() {
            Bukkit.getPluginManager().registerEvents(this, Zombies.getInstance());
        }

        @EventHandler
        private void onProjectileHit(ProjectileHitEvent event) {
            Entity hit = event.getHitEntity();

            if(hit instanceof Mob hitMob) {
                if(event.getEntity() instanceof WitherSkull skull) {
                    Optional<MetadataValue> optDuration = MetadataHelper.getMetadataValue(skull, Zombies.getInstance(),
                            EFFECT_DURATION);

                    Optional<MetadataValue> optAmplifier = MetadataHelper.getMetadataValue(skull, Zombies.getInstance(),
                            EFFECT_AMPLIFIER);

                    if(optDuration.isPresent() && optDuration.get().value() instanceof Integer duration &&
                    optAmplifier.isPresent() && optAmplifier.get().value() instanceof Integer amplifier) {
                        hitMob.removePotionEffect(PotionEffectType.WITHER);
                        hitMob.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, amplifier));
                    }
                }
            }
        }
    }

    private final float velocity;
    private final int effectDuration;
    private final int effectAmplifier;
    private final boolean isCharged;

    static {
        new Handler();
    }

    public ShootSkullMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        velocity = mlc.getFloat("velocity", 1F);
        effectDuration = mlc.getInteger("effectDuration", 20);
        effectAmplifier = mlc.getInteger("effectAmplifier", 1);
        isCharged = mlc.getBoolean("isCharged", false);
    }

    @Override
    public boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena,
                                @NotNull ZombiesPlayer target) {
        Player player = target.getPlayer();
        if(player != null && skillMetadata.getCaster().getEntity().getBukkitEntity() instanceof Mob caster) {
            Vector direction = player.getEyeLocation().toVector().subtract(caster.getEyeLocation().toVector()).normalize();

            WitherSkull skull = arena.getWorld().spawn(caster.getEyeLocation(), WitherSkull.class);
            skull.setVelocity(direction.multiply(velocity));
            skull.setCharged(isCharged);

            MetadataHelper.setFixedMetadata(skull, Zombies.getInstance(), EFFECT_DURATION, effectDuration);
            MetadataHelper.setFixedMetadata(skull, Zombies.getInstance(), EFFECT_AMPLIFIER, effectAmplifier);
            return true;
        }

        return false;
    }
}
