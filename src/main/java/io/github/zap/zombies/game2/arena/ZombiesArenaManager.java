package io.github.zap.zombies.game2.arena;

import io.github.zap.arenaapi.world.WorldLoader;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game2.arena.cleanup.PlayerCleanup;
import io.github.zap.zombies.game2.arena.stage.CountdownTickListener;
import io.github.zap.zombies.game2.arena.stage.Stage;
import io.github.zap.zombies.game2.arena.stage.StageRequirement;
import io.github.zap.zombies.game2.arena.stage.StageTransitions;
import io.github.zap.zombies.game2.arena.stage.countdown.Countdown;
import io.github.zap.zombies.game2.arena.stage.postgame.Postgame;
import io.github.zap.zombies.game2.arena.stage.pregame.Pregame;
import io.github.zap.zombies.game2.player.PlayerList;
import io.github.zap.zombies.game2.player.ZombiesPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class ZombiesArenaManager {

    private final Collection<PlayerCleanup> playerCleanups;

    private final WorldLoader worldLoader;

    public ZombiesArenaManager(@NotNull Collection<PlayerCleanup> playerCleanups, @NotNull WorldLoader worldLoader) {
        this.playerCleanups = playerCleanups;
        this.worldLoader = worldLoader;
    }

    public void createArena(@NotNull World world, @NotNull MapData mapData, @NotNull List<Player> players) {
        PlayerList<ZombiesPlayer> playerList = new PlayerList<>();

        Stage pregame = new Pregame(new StageRequirement() {
            @Override
            public boolean isMet() {
                return false; /* player count < minimum players */
            }
        });
        Stage countdown = new Countdown(new StageRequirement() {
            @Override
            public boolean isMet() {
                return false; /* player count >= minimum players */
            }
        }, List.of(
                new CountdownTickListener() {
                    @Override
                    public void onTick(int ticks) {
                        // send messages
                    }
                }
        ), mapData.getCountdownSeconds() * 20 /* TODO: make this ticks */);
        Stage postgame = new Postgame(playerList, Collections.emptyList(),
                mapData.getCountdownSeconds() * 20 /* TODO: this isn't even right */);

        StageTransitions transitions = new StageTransitions(List.of(pregame, countdown));
    }

}
