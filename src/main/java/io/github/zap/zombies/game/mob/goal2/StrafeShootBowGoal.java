package io.github.zap.zombies.game.mob.goal2;

import com.destroystokyo.paper.entity.RangedEntity;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@MythicAIGoal(name = "unboundedArrowAttackWithStrafe")
public class StrafeShootBowGoal extends PlayerTargetingGoal {
    private final RangedEntity rangedMob;

    private final double shootRangeSquared;
    private final int attackInterval;

    private int sightCounter = 0;
    private int combatCounter = 0;
    private int attackCounter = -1;

    private boolean strafeLeft;
    private boolean strafeBackwards;

    public StrafeShootBowGoal(@NotNull AbstractEntity entity, @NotNull String line,
                              @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
        this.shootRangeSquared = mlc.getDouble("shootRangeSquared", 225D);
        this.attackInterval = mlc.getInteger("shootInterval", 20);

        rangedMob = (RangedEntity) mob;
    }

    @Override
    protected void stop() {
        super.stop();

        this.sightCounter = 0;
        this.attackCounter = -1;
        mob.clearActiveItem();
    }

    @Override
    protected boolean canStart() {
        ItemStack activeItem = rangedMob.getActiveItem();
        if(activeItem != null) {
            return super.canStart() && activeItem.getType() == Material.BOW;
        }

        return false;
    }

    @Override
    protected boolean canStop() {
        ItemStack activeItem = rangedMob.getActiveItem();
        if(activeItem != null) {
            return super.canStop() || activeItem.getType() == Material.BOW;
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();

        ZombiesPlayer target = getTarget();
        Player bukkitPlayer;
        if(target != null && (bukkitPlayer = target.getPlayer()) != null) {
            double distanceSquared = rangedMob.getLocation().distanceSquared(bukkitPlayer.getLocation());
            boolean hasSight = rangedMob.hasLineOfSight(bukkitPlayer);

            if(hasSight != sightCounter > 0) {
                sightCounter = 0;
            }

            if(hasSight) {
                sightCounter++;
            }
            else {
                sightCounter--;
            }

            if(distanceSquared < shootRangeSquared && sightCounter >= 20) {
                combatCounter++;
            }
            else {
                combatCounter = -1;
            }

            if(combatCounter >= 20) {
                if(RNG.nextFloat() < 0.3D) {
                    strafeLeft = !strafeLeft;
                }

                if(RNG.nextFloat() < 0.3D) {
                    strafeBackwards = !strafeBackwards;
                }

                combatCounter = 0;
            }

            if(combatCounter > -1) {
                if (distanceSquared > shootRangeSquared * 0.75D) {
                    strafeBackwards = false;
                } else if (distanceSquared < shootRangeSquared * 0.25D) {
                    strafeBackwards = true;
                }

                zombiesNMS.entityBridge().strafe(rangedMob, strafeBackwards ? -0.5F : 0.5F, strafeLeft ? 0.5F : -0.5F);
            }

            rangedMob.lookAt(bukkitPlayer);

            if(rangedMob.isHandRaised()) {
                if(!hasSight && sightCounter < -60) {
                    rangedMob.clearActiveItem();
                }
                else if (hasSight && distanceSquared < shootRangeSquared) {
                    int itemStage = zombiesNMS.entityBridge().getTicksUsingItem(rangedMob);
                    if (itemStage >= 20) {
                        rangedMob.clearActiveItem();
                        rangedMob.rangedAttack(bukkitPlayer, zombiesNMS.entityBridge().getCharge(itemStage));
                        attackCounter = attackInterval;
                    }
                }
            }
            else if(--this.attackCounter <= 0 && sightCounter >= -60) {
                zombiesNMS.entityBridge().setCurrentHandHoldingBow(rangedMob);
            }
        }
    }
}