package io.github.zap.zombies.game.mob.mechanic;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@MythicMechanic(
        name = "spawnMobs",
        description = "General skill used for spawning mobs in Zombies games."
)
public class SpawnMobMechanic extends ZombiesArenaSkill implements Listener {
    private static final String PARENT = "skill.spawnmobs.parent";
    private static final String CHILDREN = "skill.spawnmobs.children";

    private final String mobType;
    private final int mobCountMin;
    private final int mobCountMax;
    private final int mobCap;
    private final boolean useSpawnpoints;
    private final boolean ignoreSpawnrule;
    private final double spawnRadiusSquared;
    private final double originRadiusSquared;

    public SpawnMobMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        mobType = mlc.getString("mobType");
        mobCountMin = mlc.getInteger("mobCountMin", 1);
        mobCountMax = mlc.getInteger("mobCountMax", 1);
        mobCap = mlc.getInteger("mobCap", 5);
        useSpawnpoints = mlc.getBoolean("useSpawnpoints", false);
        ignoreSpawnrule = mlc.getBoolean("ignoreSpawnrule", false);
        spawnRadiusSquared = mlc.getDouble("slaRadiusSquared", 4096);
        originRadiusSquared = mlc.getDouble("originRadiusSquared", 1024);

        Zombies.getInstance().getServer().getPluginManager().registerEvents(this, Zombies.getInstance());
    }

    @Override
    public boolean cast(@NotNull SkillMetadata metadata, @NotNull ZombiesArena arena) {
        AbstractEntity caster = metadata.getCaster().getEntity();
        MetadataValue value = MetadataHelper.computeFixedValueIfAbsent(caster.getBukkitEntity(), Zombies.getInstance(),
                CHILDREN + "." + mobType, (str) -> new HashSet<>());

        //noinspection unchecked
        Set<UUID> childMobs = (Set<UUID>) value.value();
        if(childMobs != null && childMobs.size() < mobCap) {
            int limit = mobCap - childMobs.size();
            int rngBound = mobCountMax - mobCountMin;
            int spawnAmount = Math.min(limit, mobCountMin + (int)((double)rngBound * Math.random()));

            if(useSpawnpoints) {
                List<ActiveMob> spawned = arena.getSpawner().spawnMobs(List.of(new SpawnEntryData(mobType, spawnAmount)),
                        ignoreSpawnrule ? SpawnMethod.IGNORE_SPAWNRULE : SpawnMethod.RANGED, spawnpointData ->
                                caster.getBukkitEntity().getLocation().toVector().distanceSquared(
                                        spawnpointData.getSpawn()) < originRadiusSquared, spawnRadiusSquared,
                        true, true);

                for(ActiveMob mob : spawned) {
                    registerMob(childMobs, mob, caster.getUniqueId());
                }
            }
            else {
                for(int i = 0; i < spawnAmount; i++) {
                    ActiveMob mob = arena.getSpawner().spawnMobAt(mobType, caster.getBukkitEntity()
                            .getLocation().toVector(), true);

                    if(mob != null) {
                        registerMob(childMobs, mob, caster.getUniqueId());
                    }
                }
            }

            return true;
        }

        return false;
    }

    @EventHandler
    private void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        Optional<MetadataValue> ownerOptional = MetadataHelper.getMetadataValue(event.getEntity(),
                Zombies.getInstance(), PARENT);

        if(ownerOptional.isPresent()) {
            UUID owner = (UUID)ownerOptional.get().value();
            if(owner != null) {
                Entity ownerEntity = Bukkit.getEntity(owner);

                if(ownerEntity != null) {
                    UUID self = event.getEntity().getUniqueId();
                    Optional<MetadataValue> childrenOptional = MetadataHelper.getMetadataValue(ownerEntity,
                            Zombies.getInstance(), CHILDREN + "." + mobType);

                    Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(self);
                    if(activeMob.isPresent() && activeMob.get().getMobType().equals(mobType)) {
                        Set<UUID> childMobs;
                        //noinspection unchecked
                        if(childrenOptional.isPresent() && (childMobs = (Set<UUID>)childrenOptional.get().value()) != null) {
                            childMobs.remove(self);
                        }
                    }
                }
            }
        }
    }

    private void registerMob(Set<UUID> addTo, ActiveMob spawned, UUID ownerId) {
        MetadataHelper.setFixedMetadata(spawned.getEntity().getBukkitEntity(), Zombies.getInstance(), PARENT, ownerId);
        addTo.add(spawned.getUniqueId());
    }
}
