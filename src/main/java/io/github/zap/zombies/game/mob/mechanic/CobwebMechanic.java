package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

@MythicMechanic(
        name = "cobweb",
        description = "Places a cobweb at the current AI target's location, as long as it has line of sight."
)
public class CobwebMechanic extends SkillMechanic implements ITargetedEntitySkill {
    private final int decayTime;
    private final double rangeSquared;

    public CobwebMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        decayTime = mlc.getInteger("decay",40);
        rangeSquared = mlc.getDouble("rangeSquared", 256);
        setAsyncSafe(false);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        SkillCaster caster = skillMetadata.getCaster();
        AbstractEntity entity = caster.getEntity();

        if(target != null && entity.hasLineOfSight(target) && entity.getLocation().distanceSquared(target.getLocation()) <= rangeSquared) {
            BukkitWorld world = (BukkitWorld)target.getLocation().getWorld();
            AbstractLocation targetLocation = target.getLocation();
            Block targetBlock = world.getBukkitWorld().getBlockAt(targetLocation.getBlockX(),
                    targetLocation.getBlockY(), targetLocation.getBlockZ());

            if(targetBlock.getType().isAir()) {
                placeCobweb(targetBlock);
                return true;
            }
            else {
                targetBlock = WorldUtils.blockRelative(targetBlock, new Vector(0, 1, 0)); //check block above

                if(targetBlock.getType().isAir()) { //place block at player's head, if it's currently air
                    placeCobweb(targetBlock);
                    return true;
                }
            }
        }

        return false;
    }

    private void placeCobweb(Block targetBlock) {
        BlockData save = targetBlock.getBlockData();
        targetBlock.setType(Material.COBWEB, false);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Zombies.getInstance(), () -> targetBlock.setBlockData(save),
                decayTime);
    }
}
