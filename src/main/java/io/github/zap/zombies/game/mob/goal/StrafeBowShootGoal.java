package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

@MythicAIGoal(name = "zombiesStrafeBowShoot")
public class StrafeBowShootGoal extends RangedGoal {
    private int sightCounter = 0;
    private int combatCounter = 0;
    private int attackCounter = -1;

    private boolean strafeLeft;
    private boolean strafeBackwards;

    public StrafeBowShootGoal(@NotNull AbstractEntity entity, @NotNull String line,
                              @NotNull MythicLineConfig mlc) {
        super(Zombies.getInstance(), entity, line, mlc);
    }

    private boolean isHoldingBow() {
        EntityEquipment equipment = rangedEntity.getEquipment();
        if(equipment != null) {
            return equipment.getItemInMainHand().getType() == Material.BOW;
        }

        return false;
    }

    private boolean canBeginInternal() {
        if(super.canBegin()) {
            return isHoldingBow();
        }

        return false;
    }

    @Override
    protected boolean canBegin() {
        return canBeginInternal();
    }

    @Override
    protected boolean canStop() {
        return !canBeginInternal();
    }

    @Override
    protected void stop() {
        super.stop();

        this.sightCounter = 0;
        this.attackCounter = -1;
        rangedEntity.clearActiveItem();
    }

    @Override
    public void tick() {
        super.tick();

        Player targetPlayer = getCurrentTarget().getPlayer();
        if(targetPlayer != null) {
            double distanceSquared = rangedEntity.getLocation().distanceSquared(targetPlayer.getLocation());
            boolean hasSight = rangedEntity.hasLineOfSight(targetPlayer);

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
                if(Math.random() < 0.3D) {
                    strafeLeft = !strafeLeft;
                }

                if(Math.random() < 0.3D) {
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

                zombiesNMS.entityBridge().strafe(rangedEntity, strafeBackwards ? -0.5F : 0.5F, strafeLeft ? 0.5F : -0.5F);
            }

            rangedEntity.lookAt(targetPlayer);

            if(rangedEntity.isHandRaised()) {
                if(!hasSight && sightCounter < -60) {
                    rangedEntity.clearActiveItem();
                }
                else if (hasSight && distanceSquared < shootRangeSquared) {
                    int itemStage = zombiesNMS.entityBridge().getTicksUsingItem(rangedEntity);
                    if (itemStage >= 20) {
                        rangedEntity.clearActiveItem();
                        rangedEntity.rangedAttack(targetPlayer, zombiesNMS.entityBridge().getCharge(itemStage));
                        attackCounter = shootInterval;
                    }
                }
            }
            else if(--this.attackCounter <= 0 && sightCounter >= -60) {
                zombiesNMS.entityBridge().setCurrentHandHoldingBow(rangedEntity);
            }
        }
    }
}