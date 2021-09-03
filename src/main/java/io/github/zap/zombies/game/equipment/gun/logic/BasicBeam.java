package io.github.zap.zombies.game.equipment.gun.logic;

import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represents a beam used by guns
 */
@Getter
public class BasicBeam {
    private static final double HITBOX_EXPANSION = 0.1;

    @RequiredArgsConstructor
    protected class BeamDamageAttempt implements DamageAttempt {
        private final boolean isHeadshot;

        @Override
        public int getCoins(@NotNull Damager damager, @NotNull Mob target) {
            return isHeadshot ? goldPerHeadshot : goldPerShot;
        }

        @Override
        public double damageAmount(@NotNull Damager damager, @NotNull Mob target) {
            return damage;
        }

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return isHeadshot;
        }

        @Override
        public @NotNull Vector directionVector(@NotNull Damager damager, @NotNull Mob target) {
            return directionVector.clone();
        }

        @Override
        public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
            return knockbackFactor;
        }
    }

    private final BukkitAPIHelper bukkitAPIHelper;

    private final MapData mapData;
    private final ZombiesPlayer zombiesPlayer;
    private final World world;
    private final Vector root;
    private final Vector directionVector;
    private final double distance;
    private final int maxPierceableEntities;
    private final int range;
    private final double damage;
    private final double knockbackFactor;
    private final int goldPerShot;
    private final int goldPerHeadshot;


    public BasicBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, LinearGunLevel level) {
        this.bukkitAPIHelper = MythicMobs.inst().getAPIHelper();

        this.mapData = mapData;
        this.zombiesPlayer = zombiesPlayer;
        this.world = root.getWorld();
        this.root = root.toVector();
        this.directionVector = root.getDirection().clone();

        this.maxPierceableEntities = level.getMaxPierceableEntities();
        this.range = Math.min(120, level.getRange());
        this.damage = level.getDamage();
        this.knockbackFactor = level.getKnockbackFactor();
        this.goldPerShot = level.getGoldPerShot();
        this.goldPerHeadshot = level.getGoldPerHeadshot();

        this.distance = calculateDistance();
    }

    /**
     * Calculates the distance to the shot's target block
     * @return The distance to the shot's target block
     */
    private double calculateDistance() {
        Block targetBlock = getTargetBlock();
        BoundingBox boundingBox = targetBlock.getBoundingBox();

        RayTraceResult rayTraceResult = boundingBox.rayTrace(root, directionVector,range + 1.74);
        return (rayTraceResult == null) ? range : rayTraceResult.getHitPosition().distance(root);
    }

    /**
     * Gets the targeted block of the shot
     * Adapted from {@link org.bukkit.entity.LivingEntity} CraftBukkit implementation nested calls
     * @return The targeted block
     */
    private Block getTargetBlock() {
        Block targetBlock = null;
        Iterator<Block> iterator = new BlockIterator(world, root, directionVector, 0.0D, range);

        boolean wallshot = false;
        while (iterator.hasNext()) { // TODO: don't keep looping if it's a wallshot, just get the last block
            targetBlock = iterator.next();

            if (!wallshot && !targetBlock.isPassable() && targetBlock.getType() != Material.BARRIER
                    && mapData.windowAt(targetBlock.getLocation().toVector()) == null) {
                BoundingBox boundingBox = targetBlock.getBoundingBox();
                if (boundingBox.getWidthX() != 1.0D
                        || boundingBox.getHeight() != 1.0D || boundingBox.getWidthZ() != 1.0D) {
                    if (mapData.isAllowWallshooting()) {
                        wallshot = true;
                    } else {
                        RayTraceResult rayTraceResult = boundingBox.rayTrace(root, directionVector,
                                range + 1.74);

                        if (rayTraceResult != null) {
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        return targetBlock;
    }

    /**
     * Sends the bullet
     */
    public void send() {
        hitScan();
    }

    /**
     * Performs a hitscan calculations on the entities to target
     */
    protected void hitScan() {
        List<Pair<RayTraceResult, Double>> hits = rayTrace();

        if (hits.size() > 0) {
            getZombiesPlayer().getArena().getStatsManager().queueCacheRequest(CacheInformation.PLAYER,
                    zombiesPlayer.getOfflinePlayer().getUniqueId(), PlayerGeneralStats::new,
                    (stats) -> stats.setBulletsHit(stats.getBulletsHit() + 1));

            for (Pair<RayTraceResult, Double> rayTraceResult : rayTrace()) {
                damageEntity(rayTraceResult.getLeft());
            }
        }
    }

    /**
     * Gets all the ray trace results hit by the bullet's ray trace
     * @return The ray traces of the entities that should be hit by the bullet
     */
    private List<Pair<RayTraceResult, Double>> rayTrace() {
        if (maxPierceableEntities == 0) {
            return Collections.emptyList();
        } else {
            Collection<Entity> entities = getNearbyEntities();

            List<Pair<RayTraceResult, Double>> rayTraceResults = new ArrayList<>(entities.size());
            for (Entity entity : entities) {
                BoundingBox entityBoundingBox = entity.getBoundingBox().expand(HITBOX_EXPANSION);
                RayTraceResult hitResult = entityBoundingBox.rayTrace(root, directionVector, distance);

                if (hitResult != null) {
                    Vector hitPosition = hitResult.getHitPosition();
                    rayTraceResults.add(Pair.of(
                            new RayTraceResult(hitPosition, entity, hitResult.getHitBlockFace()),
                            root.distanceSquared(hitPosition)
                    ));
                }
            }

            rayTraceResults.sort(Comparator.comparingDouble(Pair::getRight));
            return rayTraceResults.subList(0, Math.min(rayTraceResults.size(), maxPierceableEntities));
        }
    }

    /**
     * Gets an collection of all the entities near the bullet's path
     * @return The bullet
     */
    private Collection<Entity> getNearbyEntities() {
        Vector dir = directionVector.clone().normalize().multiply(distance);

        Set<UUID> entitySet = getZombiesPlayer().getArena().getEntitySet();
        Predicate<Entity> filter = (Entity entity) -> entitySet.contains(entity.getUniqueId());

        BoundingBox aabb = BoundingBox.of(root, root).expandDirectional(dir).expand(HITBOX_EXPANSION);
        return world.getNearbyEntities(aabb, filter);
    }

    /**
     * Damages an entity from a ray trace
     * @param rayTraceResult The ray trace result to get the entity from
     */
    protected void damageEntity(RayTraceResult rayTraceResult) {
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            ZombiesArena arena = getZombiesPlayer().getArena();

            boolean isHeadshot = determineIfHeadshot(rayTraceResult, mob);

            if (isHeadshot) {
                arena.getStatsManager().queueCacheRequest(CacheInformation.PLAYER,
                        getZombiesPlayer().getOfflinePlayer().getUniqueId(), PlayerGeneralStats::new,
                        (stats) -> stats.setHeadShots(stats.getHeadShots() + 1));
            }

            arena.getDamageHandler().damageEntity(getZombiesPlayer(),
                    new BeamDamageAttempt(isHeadshot), mob);

        }
    }

    /**
     * Determines whether or not a bullet was a headshot
     * @param rayTraceResult The ray trace of the bullet
     * @param mob The targeted mob
     * @return Whether or not the shot was a headsh5ot
     */
    protected boolean determineIfHeadshot(RayTraceResult rayTraceResult, Mob mob) {
        double mobY = mob.getLocation().getY();
        double eyeY = mobY + mob.getEyeHeight();
        double heightY = mobY + mob.getHeight();

        Vector hitPosition = rayTraceResult.getHitPosition();
        double yPos = hitPosition.getY();

        return (2 * eyeY - heightY <= yPos && yPos <= heightY);
    }
}
