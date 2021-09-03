package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@MythicMechanic(
        name = "teleportBehindTarget",
        description = "Teleports behind the current target. Nothing personal, kid."
)
public class TeleportBehindTargetMechanic extends ZombiesPlayerSkill {
    private final double distanceBehind;

    public TeleportBehindTargetMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        distanceBehind = mlc.getDouble("distanceBehind", 8D);
    }

    @Override
    public boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena,
                                @NotNull ZombiesPlayer target) {
        Location playerLoc = target.getPlayer().getLocation();
        Vector playerVec = playerLoc.toVector();
        Vector pDirVec = playerLoc.getDirection();
        Vector opposite = pDirVec.clone().multiply(-1);
        Vector tpVector = playerVec.add(opposite.multiply(distanceBehind));

        RayTraceResult check = arena.getWorld().rayTraceBlocks(playerLoc, opposite, distanceBehind, FluidCollisionMode.ALWAYS, false);

        AbstractLocation tpLocation;
        if(check == null) {
            tpLocation = new AbstractLocation(new BukkitWorld(arena.getWorld()), tpVector.getX(), tpVector.getY(),
                    tpVector.getZ());
        }
        else {
            double thiccness = skillMetadata.getCaster().getEntity().getBukkitEntity().getWidth();

            Vector hitPos = check.getHitPosition();
            hitPos.add(pDirVec.multiply(new Vector(thiccness, skillMetadata.getCaster().getEntity().getBukkitEntity().getHeight(), thiccness)));

            tpLocation = new AbstractLocation(new BukkitWorld(arena.getWorld()), hitPos.getX(), hitPos.getY(), hitPos.getZ());
        }

        skillMetadata.getCaster().getEntity().teleport(tpLocation);
        return true;
    }
}
