package io.github.zap.zombies.game.mob.goal2;

import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerTargetingGoal extends ZombiesPathfinderGoal<ZombiesPlayer> {
    public PlayerTargetingGoal(@NotNull Plugin plugin, @NotNull AbstractEntity entity, @NotNull String line,
                               @NotNull MythicLineConfig mlc) {
        super(plugin, entity, line, mlc);
    }


    @Override
    public @Nullable ZombiesPlayer acquireTarget() {
        double closestDistance = Double.MAX_VALUE;
        ZombiesPlayer closest = null;
        for(ZombiesPlayer player : getArena().getPlayerMap().values()) {
            Player bukkitPlayer = player.getPlayer();
            if(player.isAlive() && player.isInGame() && bukkitPlayer != null) {
                double distance = mob.getLocation().distance(bukkitPlayer.getLocation());

                if(distance < closestDistance) {
                    closestDistance = distance;
                    closest = player;
                }
            }
        }

        return closest;
    }

    @Override
    protected boolean canStart() {
        ZombiesPlayer target = getTarget();
        Player bukkitPlayer = target.getPlayer();
        if(bukkitPlayer != null) {
            GameMode gameMode = bukkitPlayer.getGameMode();
            return target.isInGame() && target.isAlive() &&
                    (gameMode == GameMode.ADVENTURE || gameMode == GameMode.SURVIVAL);
        }

        return false;
    }

    @Override
    protected boolean canStop() {
        ZombiesPlayer target = getTarget();
        Player bukkitPlayer = target.getPlayer();
        if(bukkitPlayer != null) {
            GameMode gameMode = bukkitPlayer.getGameMode();
            return !target.isInGame() || !target.isAlive() ||
                    gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
        }

        return true;
    }

    @Override
    public void start() {
        zombiesNMS.entityBridge().setAggressive(mob, true);
        mob.setTarget(getTarget().getPlayer());
    }

    @Override
    protected void stop() {
        zombiesNMS.entityBridge().setAggressive(mob, false);
        mob.setTarget(null);
    }
}
