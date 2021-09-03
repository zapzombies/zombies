package io.github.zap.zombies.game.task;

import io.github.zap.zombies.game.ZombiesArena;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * A repeating task that is executed by actions in a ZombiesArena
 */
public abstract class ZombiesTask {

    protected final ZombiesArena arena;

    private final long delay;

    private final long period;

    private int taskId = -1;

    public ZombiesTask(@NotNull ZombiesArena arena, long delay, long period) {
        this.arena = arena;
        this.delay = delay;
        this.period = period;
    }

    /**
     * Starts the task
     */
    public void start() {
        if (taskId == -1) {
            taskId = arena.runTaskTimer(delay, period, this::execute).getTaskId();
        }
    }

    /**
     * Ends the task
     */
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Notifies the task of a change in state in order for the task to stop or start itself
     */
    public void notifyChange() {

    }

    /**
     * Executes the task
     */
    protected abstract void execute();

}
