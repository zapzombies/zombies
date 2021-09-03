package io.github.zap.zombies.game.util;

import lombok.Value;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * Utilities relating to raycasting entities/blocks.
 */
public final class Raycast {
    /**
     * Data class representing a single raycast hit on an entity. All of its fields are guaranteed to be non-null.
     */
    @Value
    public static class EntityResult implements Comparable<EntityResult> {
        Entity hitEntity;
        Vector hitPosition;
        BlockFace hitBlockFace;
        double distanceSquared;

        @Override
        public int compareTo(@NotNull Raycast.EntityResult o) {
            return Double.compare(o.distanceSquared, distanceSquared);
        }
    }

    /**
     * Performs a raycast in the world specified by the given Location.
     * @param origin The raycast origin
     * @param direction The direction vector, which should be a normalized (unit) vector. If the vector is not
     *                  normalized, it will be normalized by this function (unoptimal).
     * @param maxDistance The length of this ray
     * @param entityCap The number of entities that may be returned
     * @param filter The predicate that is used to filter which entities may be raycast
     * @return A sorted queue containing the entities that were hit. The queue is ordered from most distant to least
     * distant. It will never be larger in size than entityCap, but may be smaller if enough entities could not be found
     */
    public static Queue<EntityResult> sortedRayTraceEntities(@NotNull Location origin, @NotNull Vector direction, double maxDistance,
                                                             int entityCap, Predicate<Entity> filter) {
        origin.checkFinite();
        direction.checkFinite();

        if(!direction.isNormalized()) {
            direction.normalize();
        }

        Vector originVector = origin.toVector();
        return rayCastInternalSimple((List<Entity>)origin.getWorld()
                        .getNearbyEntities(BoundingBox.of(originVector, originVector).expandDirectional(direction.clone()
                                .multiply(maxDistance)), filter), filter, originVector, direction,
                maxDistance * maxDistance, entityCap);
    }

    private static EntityResult fastRaycast(@NotNull Entity entity, @NotNull Vector start, @NotNull Vector direction,
                                            double lengthSquared) {
        BoundingBox self = entity.getBoundingBox();

        double startX = start.getX();
        double startY = start.getY();
        double startZ = start.getZ();
        double dirX = direction.getX();
        double dirY = direction.getY();
        double dirZ = direction.getZ();
        double divX = 1.0D / dirX;
        double divY = 1.0D / dirY;
        double divZ = 1.0D / dirZ;
        double tMin;
        double tMax;
        BlockFace hitBlockFaceMin;
        BlockFace hitBlockFaceMax;
        if (dirX >= 0.0D) {
            tMin = (self.getMinX() - startX) * divX;
            tMax = (self.getMaxX() - startX) * divX;
            hitBlockFaceMin = BlockFace.WEST;
            hitBlockFaceMax = BlockFace.EAST;
        } else {
            tMin = (self.getMaxX() - startX) * divX;
            tMax = (self.getMinX() - startX) * divX;
            hitBlockFaceMin = BlockFace.EAST;
            hitBlockFaceMax = BlockFace.WEST;
        }

        double tyMin;
        double tyMax;
        BlockFace hitBlockFaceYMin;
        BlockFace hitBlockFaceYMax;
        if (dirY >= 0.0D) {
            tyMin = (self.getMinY() - startY) * divY;
            tyMax = (self.getMaxY() - startY) * divY;
            hitBlockFaceYMin = BlockFace.DOWN;
            hitBlockFaceYMax = BlockFace.UP;
        } else {
            tyMin = (self.getMaxY() - startY) * divY;
            tyMax = (self.getMinY() - startY) * divY;
            hitBlockFaceYMin = BlockFace.UP;
            hitBlockFaceYMax = BlockFace.DOWN;
        }

        if (!(tMin > tyMax) && !(tMax < tyMin)) {
            if (tyMin > tMin) {
                tMin = tyMin;
                hitBlockFaceMin = hitBlockFaceYMin;
            }

            if (tyMax < tMax) {
                tMax = tyMax;
                hitBlockFaceMax = hitBlockFaceYMax;
            }

            double tzMin;
            double tzMax;
            BlockFace hitBlockFaceZMin;
            BlockFace hitBlockFaceZMax;
            if (dirZ >= 0.0D) {
                tzMin = (self.getMinZ() - startZ) * divZ;
                tzMax = (self.getMaxZ() - startZ) * divZ;
                hitBlockFaceZMin = BlockFace.NORTH;
                hitBlockFaceZMax = BlockFace.SOUTH;
            } else {
                tzMin = (self.getMaxZ() - startZ) * divZ;
                tzMax = (self.getMinZ() - startZ) * divZ;
                hitBlockFaceZMin = BlockFace.SOUTH;
                hitBlockFaceZMax = BlockFace.NORTH;
            }

            if (!(tMin > tzMax) && !(tMax < tzMin)) {
                if (tzMin > tMin) {
                    tMin = tzMin;
                    hitBlockFaceMin = hitBlockFaceZMin;
                }

                if (tzMax < tMax) {
                    tMax = tzMax;
                    hitBlockFaceMax = hitBlockFaceZMax;
                }

                if (tMax < 0.0D) {
                    return null;
                }

                if (tMin * tMin > lengthSquared) {
                    return null;
                } else {
                    double t;
                    BlockFace hitBlockFace;
                    if (tMin < 0.0D) {
                        t = tMax;
                        hitBlockFace = hitBlockFaceMax;
                    } else {
                        t = tMin;
                        hitBlockFace = hitBlockFaceMin;
                    }

                    Vector hitPoint = direction.clone().multiply(t).add(start);
                    return new EntityResult(entity, hitPoint, hitBlockFace, start.distanceSquared(hitPoint));
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static Queue<EntityResult> rayCastInternalSimple(List<Entity> sampleEntities, Predicate<Entity> filter,
                                                             Vector origin, Vector direction, double lengthSquared,
                                                             int entityCap) {
        Queue<EntityResult> chain = new PriorityQueue<>();

        for(Entity entity : sampleEntities) {
            if(filter.test(entity)) { //search for hit
                EntityResult hit = fastRaycast(entity, origin, direction, lengthSquared);

                if(hit != null) {
                    chain.add(hit);

                    if(chain.size() > entityCap) {
                        chain.remove();
                    }
                }
            }
        }

        return chain;
    }
}
