package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.game.ZombiesArena;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.logging.MythicLogger;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.jetbrains.annotations.NotNull;

@MythicMechanic(
        name = "zombiesMount",
        description = "Summons and rides a mount, which is added to the current Zombies game."
)
public class SummonMountMechanic extends ZombiesArenaSkill {
    private final String mobName;
    public SummonMountMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        this.mobName = mlc.getString("mobName");
    }

    @Override
    public boolean cast(@NotNull SkillMetadata metadata, @NotNull ZombiesArena arena) {
        MythicMob mountType = getPlugin().getMobManager().getMythicMob(mobName);

        if (mountType == null) {
            MythicLogger.errorMechanicConfig(this, this.config, "The 'mob' attribute must be a valid MythicMob.");
            return false;
        } else {
            ActiveMob mount = arena.getSpawner().spawnMobAt(mobName, metadata.getCaster().getEntity().getBukkitEntity()
                    .getLocation().toVector(), true);

            if (mount == null) {
                return false;
            } else {
                mount.getEntity().setPassenger(metadata.getCaster().getEntity());
                return true;
            }
        }
    }
}
