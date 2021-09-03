package io.github.zap.zombies.nms.v1_16_R3.entity;

import io.github.zap.zombies.nms.common.entity.EntityBridge;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class EntityBridge_v1_16_R3 implements EntityBridge {
    public static final EntityBridge_v1_16_R3 INSTANCE = new EntityBridge_v1_16_R3();

    @Override
    public boolean replacePersistentGoals(@NotNull Mob mob) {
        if (((CraftMob) mob).getHandle() instanceof EntitySkeletonAbstract skeleton) {
            try {
                Field bowShootGoal = EntitySkeletonAbstract.class.getDeclaredField("b");
                Field meleeAttackGoal = EntitySkeletonAbstract.class.getDeclaredField("c");

                bowShootGoal.setAccessible(true);
                meleeAttackGoal.setAccessible(true);

                bowShootGoal.set(skeleton, new PathfinderGoalBowShoot<>(skeleton, 0, 0, 0) {
                    @Override
                    public boolean a() {
                        return false;
                    }
                });
                meleeAttackGoal.set(skeleton, new PathfinderGoalMeleeAttack(skeleton, 0, false) {
                    @Override
                    public boolean a() {
                        return false;
                    }
                });
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setAggressive(@NotNull Mob mob, boolean aggressive) {
        ((CraftMob) mob).getHandle().setAggressive(aggressive);
    }

    @Override
    public void strafe(@NotNull Mob mob, float forward, float sideways) {
        ((CraftMob) mob).getHandle().getControllerMove().a(forward, sideways);
    }

    @Override
    public int getTicksUsingItem(@NotNull LivingEntity livingEntity) {
        return ((CraftLivingEntity) livingEntity).getHandle().ea();
    }

    @Override
    public float getCharge(int ticks) {
        return ItemBow.a(ticks);
    }

    @Override
    public void startPullingBow(@NotNull LivingEntity livingEntity) {
        EntityLiving nmsLivingEntity = ((CraftLivingEntity) livingEntity).getHandle();
        nmsLivingEntity.c(ProjectileHelper.a(nmsLivingEntity, Items.BOW));
    }

    @Override
    public boolean isAbstractSkeleton(@NotNull Entity entity) {
        return entity instanceof Skeleton;
    }

    @Override
    public @NotNull Piglin makeDream(@NotNull World world) {
        return (Piglin) new EntityPiglin(EntityTypes.PIGLIN, ((CraftWorld) world).getHandle()) {
            {
                setInvulnerable(true);
                setPersistent();
                setNoAI(true);
            }

            @Nullable
            @Override
            public GroupDataEntity prepare(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity, @Nullable NBTTagCompound nbttagcompound) {
                return null;
            }

            @Override
            protected void a(DifficultyDamageScaler difficultydamagescaler) {

            }

            @Override
            public EnumInteractionResult b(EntityHuman entityhuman, EnumHand enumhand) {
                return EnumInteractionResult.PASS;
            }

            @Override
            protected void mobTick() {

            }

            @Override
            public boolean damageEntity(DamageSource damagesource, float f) {
                return false;
            }

            @Override
            protected void b(EntityItem entityitem) {

            }

            @Override
            public boolean isCollidable() {
                return false;
            }
        }.getBukkitEntity();
    }

    @Override
    public void spawnDream(@NotNull Piglin dream, @NotNull World world) {
        ((CraftWorld) world).addEntity(((CraftEntity) dream).getHandle(), CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public void shootProjectile(@NotNull LivingEntity mob, @NotNull LivingEntity target, float idk) {
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) mob;

        if(craftLivingEntity.getHandle() instanceof IRangedEntity rangedEntity) {
            rangedEntity.a(((CraftLivingEntity)target).getHandle(), idk);
        }
    }
}
