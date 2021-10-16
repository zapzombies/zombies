package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.MetadataKeys;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@MythicMechanic(
        name = "zombiesArrowBarrage",
        description = "Summons a configurable barrage of arrows."
)
public class ArrowBarrageMechanic extends ZombiesPlayerSkill {
    private final int arrowCount;
    private final float velocity;
    private final int onFireDuration;
    private final int fireInterval;
    private final int arrowLifetime;

    public ArrowBarrageMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        arrowCount = mlc.getInteger("arrowCount", 10);
        velocity = mlc.getFloat("velocity", 1F);
        onFireDuration = mlc.getInteger("onFireDuration", 0);
        fireInterval = mlc.getInteger("fireInterval", 5);
        arrowLifetime = mlc.getInteger("arrowLifetime", 100);
    }

    @Override
    public boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena,
                                @NotNull ZombiesPlayer target) {
        Player player = target.getPlayer();

        if(skillMetadata.getCaster().getEntity().getBukkitEntity() instanceof Mob caster && player != null &&
                !hasTask(caster)) {
            caster.playEffect(EntityEffect.ENTITY_POOF);
            Vector direction = player.getEyeLocation().toVector().subtract(caster.getLocation().toVector()).normalize();

            List<Arrow> arrows = new ArrayList<>();
            AtomicInteger count = new AtomicInteger();
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(Zombies.getInstance(), () -> {
                Arrow arrow = player.getWorld().spawnArrow(caster.getEyeLocation(), direction, velocity, 0);
                arrows.add(arrow);
                arrow.setShooter(caster);

                if(onFireDuration > 0) {
                    arrow.setFireTicks(onFireDuration);
                }

                if(count.incrementAndGet() == arrowCount) {
                    BukkitTask bukkitTask = MetadataHelper.getMetadataInstance(caster, Zombies.getInstance(),
                            MetadataKeys.SKILL_ARROWBARRAGE.getKey());
                    bukkitTask.cancel();

                    caster.removeMetadata(MetadataKeys.SKILL_ARROWBARRAGE.getKey(), Zombies.getInstance());

                    Bukkit.getScheduler().runTaskLater(Zombies.getInstance(), () -> {
                        for(Arrow spawned : arrows) {
                            spawned.remove();
                        }
                    }, arrowLifetime);
                }
            }, 0, fireInterval);

            MetadataHelper.setFixedMetadata(caster, Zombies.getInstance(), MetadataKeys.SKILL_ARROWBARRAGE.getKey(),
                    task);
            return true;
        }

        return false;
    }

    private boolean hasTask(Mob mob) {
        Optional<MetadataValue> taskOptional = MetadataHelper.getMetadataValue(mob, Zombies.getInstance(),
                MetadataKeys.SKILL_ARROWBARRAGE.getKey());
        return taskOptional.filter(metadataValue -> metadataValue.value() != null).isPresent();
    }
}
