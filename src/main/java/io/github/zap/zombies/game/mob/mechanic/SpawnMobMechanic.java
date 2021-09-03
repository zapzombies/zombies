package io.github.zap.zombies.game.mob.mechanic;

import com.google.common.collect.ImmutableList;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
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
    private static final String OWNER_METADATA_NAME = "spawn_owner";

    private final String mobType;
    private final int mobCountMin;
    private final int mobCountMax;
    private final int mobCap;
    private final boolean useSpawnpoints;
    private final boolean ignoreSpawnrule;
    private final double spawnRadiusSquared;
    private final double originRadiusSquared;

    private static final Map<UUID, Set<UUID>> mobs = new HashMap<>();
    private static final Random RNG = new Random();

    public SpawnMobMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        super.setAsyncSafe(false);
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
        Set<UUID> spawnedMobs = mobs.computeIfAbsent(caster.getUniqueId(), uuid -> new HashSet<>());

        if(spawnedMobs.size() < mobCap) {
            int limit = mobCap - spawnedMobs.size();
            int rngBound = mobCountMax - mobCountMin;
            int spawnAmount = Math.min(limit, mobCountMin + ((rngBound <= 0) ? 0 : RNG.nextInt(rngBound)));

            if(useSpawnpoints) {
                List<ActiveMob> spawned = arena.getSpawner().spawnMobs(ImmutableList.of(new SpawnEntryData(mobType, spawnAmount)),
                        ignoreSpawnrule ? SpawnMethod.IGNORE_SPAWNRULE : SpawnMethod.RANGED, spawnpointData ->
                                caster.getBukkitEntity().getLocation().toVector().distanceSquared(
                                        spawnpointData.getSpawn()) < originRadiusSquared, spawnRadiusSquared,
                        true, true);

                for(ActiveMob mob : spawned) {
                    registerMob(spawnedMobs, mob, caster.getUniqueId());
                }
            }
            else {
                for(int i = 0; i < spawnAmount; i++) {
                    ActiveMob mob = arena.getSpawner().spawnMobAt(mobType, caster.getBukkitEntity().getLocation()
                            .toVector(), true);

                    if(mob != null) {
                        registerMob(spawnedMobs, mob, caster.getUniqueId());
                    }
                }
            }

            return true;
        }

        return false;
    }

    @EventHandler
    private void onMythicMobDeath(MythicMobDeathEvent event) {
        UUID deadUUID = event.getEntity().getUniqueId();

        if(mobs.remove(deadUUID) == null) { //if the caster died, remove its spawned instances
            MetadataValue value = MetadataHelper.getMetadataFor(event.getEntity(), Zombies.getInstance(), OWNER_METADATA_NAME);
            if(value != null) {
                UUID owner = (UUID)value.value();

                if(owner != null) {
                    Set<UUID> spawned = mobs.get(owner);

                    if(spawned != null) {
                        spawned.remove(deadUUID);

                        if(spawned.isEmpty()) {
                            mobs.remove(owner);
                        }
                    }
                }
            }
        }
    }

    private void registerMob(Set<UUID> registerTo, ActiveMob mob, UUID ownerId) {
        registerTo.add(mob.getUniqueId());
        MetadataHelper.setMetadataFor(mob.getEntity().getBukkitEntity(), OWNER_METADATA_NAME, Zombies.getInstance(), ownerId);
    }
}
