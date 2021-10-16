package io.github.zap.zombies.game.mob.mechanic;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.MetadataKeys;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.variables.types.EntityListVariable;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
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
    private record ShootSkullMetadata(WitherSkull skull, PotionEffectType type, int effectDuration,
                                      int effectAmplifier) {}

    private static class Handler implements Listener {
        private final Zombies zombies;

        private Handler() {
            Bukkit.getPluginManager().registerEvents(this, zombies = Zombies.getInstance());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onProjectileHit(ProjectileHitEvent event) {
            Entity hit = event.getHitEntity();

            if(hit instanceof LivingEntity hitLiving && event.getEntity() instanceof WitherSkull skull) {
                Optional<MetadataValue> shootSkullMetadata = MetadataHelper.getMetadataValue(skull, zombies,
                        MetadataKeys.SKILL_SHOOTSKULL.getKey());

                shootSkullMetadata.ifPresent(metadataValue -> {
                    Object value;
                    if((value = metadataValue.value()) != null) {
                        MetadataHelper.setFixedMetadata(hitLiving, zombies, MetadataKeys.SKILL_SHOOTSKULL.getKey(),
                                value);
                    }

                    skull.removeMetadata(MetadataKeys.SKILL_SHOOTSKULL.getKey(), zombies);
                });
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onEntityPotionEffect(EntityPotionEffectEvent event) {
            if(event.getEntity() instanceof LivingEntity living) {
                Optional<MetadataValue> value = MetadataHelper.getMetadataValue(living, zombies,
                        MetadataKeys.SKILL_SHOOTSKULL.getKey());

                if(value.isPresent()) {
                    ShootSkullMetadata skullMetadata = (ShootSkullMetadata) value.get().value();

                    if(skullMetadata != null) {
                        living.addPotionEffect(new PotionEffect(skullMetadata.type, skullMetadata.effectDuration,
                                skullMetadata.effectAmplifier));
                    }
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
            if(event.getEntity() instanceof LivingEntity living) {
                living.removeMetadata(MetadataKeys.SKILL_SHOOTSKULL.getKey(), zombies);
            }
        }
    }

    private final float speed;
    private final PotionEffectType type;
    private final int effectDuration;
    private final int effectAmplifier;
    private final boolean isCharged;

    static {
        new Handler();
    }

    public ShootSkullMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        speed = mlc.getFloat("speed", 1F);
        PotionEffectType typeTemp = PotionEffectType.getByName(mlc.getString("potionType", "WITHER"));
        type = typeTemp == null ? PotionEffectType.WITHER : typeTemp;
        effectDuration = mlc.getInteger("effectDuration", 60);
        effectAmplifier = mlc.getInteger("effectAmplifier", 1);
        isCharged = mlc.getBoolean("isCharged", false);
    }

    @Override
    public boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena,
                                @NotNull ZombiesPlayer target) {
        Player player = target.getPlayer();
        if(player != null && skillMetadata.getCaster().getEntity().getBukkitEntity() instanceof Mob caster) {
            Vector direction = player.getEyeLocation().toVector().subtract(caster.getEyeLocation().toVector())
                    .normalize();

            WitherSkull skull = arena.getWorld().spawn(caster.getEyeLocation(), WitherSkull.class);
            skull.setVelocity(direction.multiply(speed));
            skull.setCharged(isCharged);

            MetadataHelper.setFixedMetadata(skull, Zombies.getInstance(), MetadataKeys.SKILL_SPAWNMOBS.getKey(),
                    new ShootSkullMetadata(skull, type, effectDuration, effectAmplifier));
            return true;
        }

        return false;
    }
}
