package io.github.zap.zombies.game;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public record RoundContext(List<BukkitTask> spawnTasks, List<BukkitTask> speedupTasks, List<BukkitTask> removeTasks, List<ActiveMob> spawnedMobs) {
    void cancelRound() {
        for(BukkitTask task : spawnTasks) {
            task.cancel();
        }

        for(BukkitTask task : removeTasks) {
            task.cancel();
        }

        for(ActiveMob mob : spawnedMobs) {
            Entity entity = mob.getEntity().getBukkitEntity();

            if(entity != null) {
                entity.remove();
            }
        }
    }
}
