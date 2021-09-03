package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Task which kills all zombies within a certain radius of the activating player
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class DragonWrath extends TeamMachineTask implements Damager {

    private static class DragonWrathDamage implements DamageAttempt {
        @Override
        public int getCoins(@NotNull Damager damager, @NotNull Mob target) {
            return 0;
        }

        @Override
        public double damageAmount(@NotNull Damager damager, @NotNull Mob target) {
            return target.getHealth();
        }

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return true;
        }

        @Override
        public @NotNull Vector directionVector(@NotNull Damager damager, @NotNull Mob target) {
            return new Vector();
        }

        @Override
        public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
            return 0;
        }
    }

    private int costIncrement = 1000;

    private int delay = 30;

    private double radius = 15D;

    public DragonWrath() {
        super(TeamMachineTaskType.DRAGON_WRATH.name());
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        Player player = zombiesPlayer.getPlayer();
        if (super.execute(teamMachine, zombiesArena, zombiesPlayer) && player != null) {
            Location location = teamMachine.getBlock().getLocation();
            Set<UUID> mobIds = zombiesArena.getEntitySet();

            World world = zombiesArena.getWorld();
            world.playSound(Sound.sound(
                    Key.key("minecraft:entity.ender_dragon.growl"),
                    Sound.Source.MASTER,
                    1.0F,
                    1.0F
            ), location.getX(), location.getY(), location.getZ());

            zombiesArena.runTaskLater(delay, () -> {
                int entitiesKilled = 0;
                for (Mob mob : world.getNearbyEntitiesByType(Mob.class, location, radius)) {
                    if (mobIds.contains(mob.getUniqueId())) {
                        Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(mob.getUniqueId());
                        boolean resistInstakill = false;
                        if (activeMob.isPresent()) {
                            resistInstakill = activeMob.get().getType().getConfig().getBoolean("ResistInstakill",
                                    false);
                        }

                        if (!resistInstakill) {
                            world.strikeLightningEffect(mob.getLocation());
                            zombiesArena.getDamageHandler().damageEntity(
                                    DragonWrath.this,
                                    new DragonWrathDamage(),
                                    mob
                            );
                            entitiesKilled++;
                        }
                    }
                }

                zombiesPlayer.addKills(entitiesKilled);

                player.sendMessage(Component
                        .text(String.format("Killed %d mobs!", entitiesKilled))
                        .color(NamedTextColor.GREEN));
            });

            return true;
        }

        return false;
    }

    @Override
    public int getCostForTeamMachine(TeamMachine teamMachine) {
        return getInitialCost() + (costIncrement * getTimesUsed().getValue(teamMachine));
    }

    @Override
    public void onDealsDamage(@NotNull DamageAttempt item, @NotNull Mob damaged, double deltaHealth) {

    }
}
