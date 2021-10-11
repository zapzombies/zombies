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
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@MythicMechanic(
        name = "zombiesShootSkull",
        description = "Shoots a Wither skull at the player, which applies a wither effect with an adjustable duration."
)
public class ShootSkullMechanic extends ZombiesPlayerSkill implements Listener {
    private static final String EFFECT_DURATION = "skill.shootskull.duration";
    private static final String EFFECT_AMPLIFIER = "skill.shootskull.amplifier";
    private static final String EFFECT_SKULL = "skill.shootskull.skull";

    private static class Handler implements Listener {
        private Handler() {
            Bukkit.getPluginManager().registerEvents(this, Zombies.getInstance());
        }

        @EventHandler
        private void onProjectileHit(ProjectileHitEvent event) {
            Entity hit = event.getHitEntity();

            if(hit instanceof LivingEntity hitLiving && event.getEntity() instanceof WitherSkull skull) {
                if(skull.hasMetadata(EFFECT_DURATION) && skull.hasMetadata(EFFECT_AMPLIFIER)) {
                    MetadataHelper.setFixedMetadata(hitLiving, Zombies.getInstance(), EFFECT_SKULL, skull);
                }
            }
        }

        @EventHandler
        private void onEntityPotionEffect(EntityPotionEffectEvent event) {
            if(event.getEntity() instanceof LivingEntity living) {
                Optional<MetadataValue> value = MetadataHelper.getMetadataValue(living, Zombies.getInstance(),
                        EFFECT_SKULL);

                if(value.isPresent() && value.get().value() instanceof WitherSkull skull) {
                    Optional<MetadataValue> optDuration = MetadataHelper.getMetadataValue(skull, Zombies.getInstance(),
                            EFFECT_DURATION);

                    Optional<MetadataValue> optAmplifier = MetadataHelper.getMetadataValue(skull, Zombies.getInstance(),
                            EFFECT_AMPLIFIER);

                    if(optDuration.isPresent() && optDuration.get().value() instanceof Integer duration &&
                        optAmplifier.isPresent() && optAmplifier.get().value() instanceof Integer amplifier) {
                        living.removeMetadata(EFFECT_SKULL, Zombies.getInstance());
                        event.setCancelled(true);

                        living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, amplifier));
                    }
                }
            }
        }
    }

    private final float speed;
    private final int effectDuration;
    private final int effectAmplifier;
    private final boolean isCharged;

    static {
        new Handler();
    }

    public ShootSkullMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        speed = mlc.getFloat("speed", 1F);
        effectDuration = mlc.getInteger("effectDuration", 60);
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
            skull.setVelocity(direction.multiply(speed));
            skull.setCharged(isCharged);

            MetadataHelper.setFixedMetadata(skull, Zombies.getInstance(), EFFECT_DURATION, effectDuration);
            MetadataHelper.setFixedMetadata(skull, Zombies.getInstance(), EFFECT_AMPLIFIER, effectAmplifier);
            return true;
        }

        return false;
    }
}
