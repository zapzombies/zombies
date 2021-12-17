package io.github.zap.zombies.game2.arena.stage.postgame;

import io.github.zap.zombies.game2.player.PlayerList;
import io.github.zap.zombies.game2.player.ZombiesPlayer;
import io.github.zap.zombies.game2.arena.stage.Effect;
import io.github.zap.zombies.game2.arena.stage.Stage;
import io.github.zap.zombies.game2.arena.stage.StageCompletion;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class Postgame implements Stage {

    private final static int COUNTDOWN_NOT_STARTED = -1;

    private final PlayerList<ZombiesPlayer> playerList;

    private final Collection<Effect> effects;

    private final int countdownTicks;

    private int ticksUntilCountdown = COUNTDOWN_NOT_STARTED;

    public Postgame(@NotNull PlayerList<ZombiesPlayer> playerList, @NotNull Collection<Effect> effects,
                    int countdownTicks) {
        this.playerList = Objects.requireNonNull(playerList, "playerList cannot be null!");
        this.effects = Objects.requireNonNull(effects, "effects cannot be null!");
        this.countdownTicks = countdownTicks;
    }

    @Override
    public void begin() {
        if (ticksUntilCountdown != COUNTDOWN_NOT_STARTED) {
            throw new IllegalStateException("Postgame already started!");
        }

        ticksUntilCountdown = countdownTicks;

        for (ZombiesPlayer player : playerList.getPlayers().values()) {
            if (!player.isInGame()) {
                continue;
            }

            player.getPlayerIfValid().ifPresent(bukkitPlayer -> {
                for (Effect effect : effects) {
                    effect.apply(bukkitPlayer);
                }
            });
        }
    }

    @Override
    public @NotNull StageCompletion tick() {
        if (ticksUntilCountdown == COUNTDOWN_NOT_STARTED) {
            throw new IllegalStateException("Postgame not yet started!");
        }

        if (ticksUntilCountdown == 0) {
            return StageCompletion.NEXT;
        }

        ticksUntilCountdown--;
        return StageCompletion.CONTINUE;
    }

    @Override
    public void cancel() {
        ticksUntilCountdown = COUNTDOWN_NOT_STARTED;
    }

    protected int getCountdownTicks() {
        return countdownTicks;
    }

    protected int getTicksUntilCountdown() {
        return ticksUntilCountdown;
    }

}
