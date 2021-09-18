package io.github.zap.zombies.game.mob.mechanic;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.google.common.collect.ImmutableList;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
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
    private static final String OWNER_METADATA = "spawn_owner";

    private final String mobType;
    private final int mobCountMin;
    private final int mobCountMax;
    private final int mobCap;
    private final boolean useSpawnpoints;
    private final boolean ignoreSpawnrule;
    private final double spawnRadiusSquared;
    private final double originRadiusSquared;

    private static final Map<UUID, Set<UUID>> owners = new HashMap<>();

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
        Set<UUID> ownedMobs = owners.computeIfAbsent(caster.getUniqueId(), uuid -> new HashSet<>());

        if(ownedMobs.size() < mobCap) {
            int limit = mobCap - ownedMobs.size();
            int rngBound = mobCountMax - mobCountMin;
            int spawnAmount = Math.min(limit, mobCountMin + (int)((double)rngBound * Math.random()));

            if(useSpawnpoints) {
                List<ActiveMob> spawned = arena.getSpawner().spawnMobs(List.of(new SpawnEntryData(mobType, spawnAmount)),
                        ignoreSpawnrule ? SpawnMethod.IGNORE_SPAWNRULE : SpawnMethod.RANGED, spawnpointData ->
                                caster.getBukkitEntity().getLocation().toVector().distanceSquared(
                                        spawnpointData.getSpawn()) < originRadiusSquared, spawnRadiusSquared,
                        true, true);

                for(ActiveMob mob : spawned) {
                    registerMob(ownedMobs, mob, caster.getUniqueId());
                }
            }
            else {
                for(int i = 0; i < spawnAmount; i++) {
                    ActiveMob mob = arena.getSpawner().spawnMobAt(mobType, caster.getBukkitEntity()
                            .getLocation().toVector(), true);

                    if(mob != null) {
                        registerMob(ownedMobs, mob, caster.getUniqueId());
                    }
                }
            }

            return true;
        }

        return false;
    }

    @EventHandler
    private void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        UUID deadUUID = event.getEntity().getUniqueId();

        if(owners.remove(deadUUID) == null) {
            Optional<MetadataValue> valueOptional = MetadataHelper.getMetadataValue(event.getEntity(),
                    Zombies.getInstance(), OWNER_METADATA);
            if(valueOptional.isPresent()) {
                UUID owner = (UUID)valueOptional.get().value();

                if(owner != null) {
                    Set<UUID> spawned = owners.get(owner);

                    if(spawned != null) {
                        spawned.remove(deadUUID);

                        if(spawned.isEmpty()) {
                            owners.remove(owner);
                        }
                    }
                }
            }
        }
    }

    private void registerMob(Set<UUID> registerTo, ActiveMob spawned, UUID ownerId) {
        registerTo.add(spawned.getUniqueId());
        MetadataHelper.setFixedMetadata(spawned.getEntity().getBukkitEntity(), Zombies.getInstance(),
                OWNER_METADATA, ownerId);
    }
}
