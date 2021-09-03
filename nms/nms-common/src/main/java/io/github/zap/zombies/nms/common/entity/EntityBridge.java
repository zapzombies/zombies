package io.github.zap.zombies.nms.common.entity;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Piglin;
import org.jetbrains.annotations.NotNull;

/**
 * A bridge for static methods from or directly relating to NMS Entity.
 */
public interface EntityBridge {

    /**
     * Replaces any persistent goals on a mob with dummy goals
     * @param mob The mob to erase goals from
     */
    boolean replacePersistentGoals(@NotNull Mob mob);

    /**
     * Sets whether a {@link Mob} is aggressive
     * @param mob The mob to set aggression on
     * @param aggressive Whether it is aggressive
     */
    void setAggressive(@NotNull Mob mob, boolean aggressive);

    /**
     * Makes a {@link Mob} strafe
     * @param mob The mob to make strafe
     * @param forward Forward strafe
     * @param sideways Sideways strafe
     */
    void strafe(@NotNull Mob mob, float forward, float sideways);

    /**
     * Gets the number of ticks a {@link LivingEntity} has used an item
     * @param livingEntity The living entity to check
     * @return The number of ticks it has used its item
     */
    int getTicksUsingItem(@NotNull LivingEntity livingEntity);

    /**
     * Gets the charge of a ranged attack
     * @param ticks The number of ticks to get the charge for
     */
    float getCharge(int ticks);

    /**
     * Makes a living entity start pulling its bow
     * @param livingEntity The affected living entity
     */
    void startPullingBow(@NotNull LivingEntity livingEntity);

    /**
     * Determines if an {@link Entity} is an abstract skeleton
     * @param entity The entity to check
     * @return Whether it is an abstract skeleton
     */
    boolean isAbstractSkeleton(@NotNull Entity entity);

    /**
     * Dream stans UNITE
     * (that's what the point of the mask is 😔)
     * @param world Dream's home
     * @return The best youtuber
     */
    @NotNull Piglin makeDream(@NotNull World world);

    /**
     * Adds dream to a world
     * @param dream The legend himself
     * @param world His lovely home
     */
    void spawnDream(@NotNull Piglin dream, @NotNull World world);

    void shootProjectile(@NotNull LivingEntity livingEntity, @NotNull LivingEntity target, float idk);
}
