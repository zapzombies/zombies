package io.github.zap.zombies.game2.arena.stage.countdown;

import io.github.zap.zombies.game2.arena.stage.CountdownTickListener;
import io.github.zap.zombies.game2.arena.stage.Stage;
import io.github.zap.zombies.game2.arena.stage.StageCompletion;
import io.github.zap.zombies.game2.arena.stage.StageRequirement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class Countdown implements Stage {

    private final static int COUNTDOWN_NOT_STARTED = -1;

    private final StageRequirement requirement;

    private final Collection<CountdownTickListener> listeners;

    private final int countdownTicks;

    private int ticksUntilCountdown = COUNTDOWN_NOT_STARTED;

    public Countdown(@NotNull StageRequirement requirement, @NotNull Collection<CountdownTickListener> listeners,
                     int countdownTicks) {
        this.requirement = Objects.requireNonNull(requirement, "requirement cannot be null!");
        this.listeners = Objects.requireNonNull(listeners, "listeners cannot be null!");
        this.countdownTicks = countdownTicks;
    }

    @Override
    public void begin() {
        if (ticksUntilCountdown != COUNTDOWN_NOT_STARTED) {
            throw new IllegalStateException("Countdown already started!");
        }

        ticksUntilCountdown = countdownTicks;
    }

    @Override
    public @NotNull StageCompletion tick() {
        if (ticksUntilCountdown == COUNTDOWN_NOT_STARTED) {
            throw new IllegalStateException("Countdown not yet started!");
        }

        if (requirement.isMet()) {
            if (ticksUntilCountdown == 0) {
                return StageCompletion.NEXT;
            }

            ticksUntilCountdown--;
            this.onTick();
            return StageCompletion.CONTINUE;
        }

        return StageCompletion.BACK;
    }

    @Override
    public void cancel() {
        ticksUntilCountdown = COUNTDOWN_NOT_STARTED;
    }

    protected void onTick() {
        for (CountdownTickListener listener : listeners) {
            listener.onTick(ticksUntilCountdown);
        }
    }

    protected int getCountdownTicks() {
        return countdownTicks;
    }

    protected int getTicksUntilCountdown() {
        return ticksUntilCountdown;
    }

}
