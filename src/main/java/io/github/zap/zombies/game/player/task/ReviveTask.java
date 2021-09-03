package io.github.zap.zombies.game.player.task;

import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.task.ZombiesTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Task to revive nearby players
 */
public class ReviveTask extends ZombiesTask {

    private final ZombiesPlayer player;

    private Corpse targetCorpse;

    private boolean reviveOn = false;

    public ReviveTask(@NotNull ZombiesPlayer player) {
        super(player.getArena(), 0L, 2L);
        this.player = player;

        arena.getProxyFor(PlayerToggleSneakEvent.class).registerHandler(args -> {
            if (args.getEvent().getPlayer().equals(player.getPlayer())) {
                reviveOn = args.getEvent().isSneaking();
            }
        });
    }

    @Override
    protected void execute() {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null && player.isAlive()) {
            if (targetCorpse == null) {
                selectNewCorpse(bukkitPlayer);
            } else if (!targetCorpse.isActive()) {
                resetTargetCorpse();
                selectNewCorpse(bukkitPlayer);
            } else {
                attemptToContinueReviving(bukkitPlayer);
            }
        } else if (targetCorpse != null) {
            resetTargetCorpse();
        }
    }

    private void attemptToContinueReviving(@NotNull Player bukkitPlayer) {
        int maxDistance = arena.getMap().getReviveRadius();
        double distance = bukkitPlayer.getLocation().toVector()
                .distanceSquared(targetCorpse.getLocation().toVector());

        if (distance < maxDistance && reviveOn) {
            targetCorpse.continueReviving();
        } else {
            resetTargetCorpse();
            selectNewCorpse(bukkitPlayer);
        }
    }

    private void resetTargetCorpse() {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            bukkitPlayer.sendActionBar(Component.empty());
        }

        if (targetCorpse != null) {
            targetCorpse.setReviver(null);
            targetCorpse = null;
        }
    }

    private void selectNewCorpse(@NotNull Player bukkitPlayer) {
        int maxDistance = arena.getMap().getReviveRadius();

        for (Corpse corpse : arena.getAvailableCorpses()) {
            double distance = bukkitPlayer.getLocation().toVector().distanceSquared(corpse.getLocation().toVector());

            if (distance <= maxDistance) {
                Player corpseBukkitPlayer = corpse.getZombiesPlayer().getPlayer();
                if (corpseBukkitPlayer != null) {
                    if (reviveOn) {
                        targetCorpse = corpse;
                        targetCorpse.setReviver(player);
                        targetCorpse.continueReviving();
                    } else {
                        bukkitPlayer.sendActionBar(Component.text(String.format("Hold SHIFT to Revive %s!",
                                corpseBukkitPlayer.getName()), NamedTextColor.YELLOW));
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void notifyChange() {
        super.notifyChange();
        if (player.isAlive()) {
            start();
        } else {
            stop();
        }
    }

    @Override
    public void stop() {
        super.stop();
        resetTargetCorpse();
    }

}
