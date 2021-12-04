package io.github.zap.zombies.game.mob.mechanic;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.commons.utils.MetadataHelper;
import io.github.zap.zombies.MetadataKeys;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@MythicMechanic(
        name = "spawnMobs",
        description = "General skill used for spawning mobs in Zombies games."
)
public class SpawnMobMechanic extends ZombiesArenaSkill implements Listener {
    private record SpawnMobMetadata(UUID parent, Map<String, Set<UUID>> mappings) {}

    private static class Handler implements Listener {
        private final Zombies zombies;

        private Handler() {
            zombies = Zombies.getInstance();
            Bukkit.getPluginManager().registerEvents(this, zombies);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
            Entity entity = event.getEntity();
            Optional<MetadataValue> spawnMobMetadataOptional = MetadataHelper.getMetadataValue(entity, zombies,
                    MetadataKeys.SKILL_SPAWNMOBS.getKey());

            if(spawnMobMetadataOptional.isPresent()) {
                SpawnMobMetadata spawnMobMetadata = (SpawnMobMetadata)spawnMobMetadataOptional.get().value();

                if(spawnMobMetadata != null) {
                    UUID owner = spawnMobMetadata.parent;

                    if(owner != null) {
                        Entity ownerEntity = Bukkit.getEntity(owner);

                        if(ownerEntity != null) {
                            UUID selfUUID = event.getEntity().getUniqueId();
                            Optional<ActiveMob> selfActive = MythicMobs.inst().getMobManager().getActiveMob(selfUUID);

                            if(selfActive.isPresent()) {
                                String mobType = selfActive.get().getMobType();

                                Optional<MetadataValue> ownerMetadataOptional = MetadataHelper.getMetadataValue(
                                        ownerEntity, zombies, MetadataKeys.SKILL_SPAWNMOBS.getKey());

                                if(ownerMetadataOptional.isPresent()) {
                                    SpawnMobMetadata ownerMeta = (SpawnMobMetadata)ownerMetadataOptional.get().value();

                                    if(ownerMeta != null) {
                                        Set<UUID> children = ownerMeta.mappings.get(mobType);
                                        if(children != null) {
                                            children.remove(selfUUID);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                entity.removeMetadata(MetadataKeys.SKILL_SPAWNMOBS.getKey(), zombies);
            }
        }
    }

    private final String mobType;
    private final int mobCountMin;
    private final int mobCountMax;
    private final int mobCap;
    private final boolean useSpawnpoints;
    private final boolean ignoreSpawnrule;
    private final double spawnRadiusSquared;
    private final double originRadiusSquared;

    static {
        new Handler();
    }

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
    }

    @Override
    public boolean cast(@NotNull SkillMetadata metadata, @NotNull ZombiesArena arena) {
        AbstractEntity caster = metadata.getCaster().getEntity();
        Entity bukkitEntity = caster.getBukkitEntity();

        SpawnMobMetadata optionalSpawnMetadata = (SpawnMobMetadata)MetadataHelper.computeFixedValueIfAbsent(bukkitEntity,
                Zombies.getInstance(), MetadataKeys.SKILL_SPAWNMOBS.getKey(), (ignored) ->
                        new SpawnMobMetadata(bukkitEntity.getUniqueId(), new HashMap<>())).value();

        if(optionalSpawnMetadata != null) {
            Set<UUID> childMobs = optionalSpawnMetadata.mappings.computeIfAbsent(mobType, (ignored) -> new HashSet<>());

            if(childMobs.size() < mobCap) {
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
                        registerMob(childMobs, mob.getEntity().getBukkitEntity(), caster.getUniqueId());
                    }
                }
                else {
                    for(int i = 0; i < spawnAmount; i++) {
                        ActiveMob mob = arena.getSpawner().spawnMobAt(mobType, caster.getBukkitEntity()
                                .getLocation().toVector(), true);

                        if(mob != null) {
                            registerMob(childMobs, mob.getEntity().getBukkitEntity(), caster.getUniqueId());
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    private void registerMob(Set<UUID> addTo, Entity spawned, UUID ownerId) {
        MetadataHelper.setFixedMetadata(spawned, Zombies.getInstance(), MetadataKeys.SKILL_SPAWNMOBS.getKey(),
                new SpawnMobMetadata(ownerId, new HashMap<>()));
        addTo.add(spawned.getUniqueId());
    }
}
