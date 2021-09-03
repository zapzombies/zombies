package io.github.zap.zombies.game.scoreboards;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static io.github.zap.zombies.game.scoreboards.GameScoreboard.DATE_FORMATTER;

public class IngameScoreboardState implements GameScoreboardState, Disposable {
    public static final long secondToMillis = 1000;
    public static final long minuteToMillis = 60 * secondToMillis;
    public static final long hourToMillis = 60 * minuteToMillis;

    private GameScoreboard gameScoreboard;

    private final Map<UUID, Pair<StringFragment, StringFragment>> playerStatues = new HashMap<>();
    private final Map<UUID, IngamePlayerScoreboardInformation> playScoreboards = new HashMap<>();

    private final StringFragment round = new StringFragment();
    private final StringFragment zombieLeft = new StringFragment();
    private final StringFragment time = new StringFragment();


    @Override
    public void stateChangedFrom(ZombiesArenaState gameState, GameScoreboard scoreboard) {
        this.gameScoreboard = scoreboard;
        var date = DATE_FORMATTER.format(LocalDateTime.now());
        var map = scoreboard.getZombiesArena().getMap().getMapDisplayName();

        for(var i : scoreboard.getZombiesArena().getPlayerMap().entrySet()) {
            var tfName = new StringFragment(i.getValue().getOfflinePlayer().getName());
            var tfState = new StringFragment();
            playerStatues.put(i.getKey(), Pair.of(tfName, tfState));
        }

        for(var player : scoreboard.getZombiesArena().getPlayerMap().entrySet()) {
            var bukkitScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            var writer = SidebarTextWriter.create(bukkitScoreboard, GameScoreboard.SIDEBAR_TITLE);
            var zombieKills = new StringFragment(ChatColor.GOLD + "0");

            // Writing the side bar
            writer.line(ChatColor.GRAY, date)
                    .line()
                    .line("" + ChatColor.BOLD + ChatColor.RED, round)
                    .line("Zombies Left: " + ChatColor.GREEN, zombieLeft)
                    .line();

            Pair<StringFragment, StringFragment> first = playerStatues.get(player.getKey());
            writer.line(ChatColor.GRAY, first.getLeft(), ChatColor.WHITE + ": ",
                    first.getRight());
            for (Map.Entry<UUID, Pair<StringFragment, StringFragment>> entry : playerStatues.entrySet()) {
                if (!entry.getKey().equals(player.getKey())) {
                    writer.line(ChatColor.GRAY, entry.getValue().getLeft(), ChatColor.WHITE + ": ",
                            entry.getValue().getRight());
                }
            }

            writer.line()
                    .line("Zombie Kills: " + ChatColor.GREEN, zombieKills)
                    .line("Time: " + ChatColor.GREEN, time)
                    .line("Map: " + ChatColor.GREEN + map)
                    .line()
                    .text(ChatColor.YELLOW + "discord.gg/:zzz:");


            // Health objective
            var objHealth = bukkitScoreboard.registerNewObjective("objHealth", Criterias.HEALTH, "Health");
            objHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);

            // Kills objective
            var objKills = bukkitScoreboard.registerNewObjective("objKills", "dummy", "Zombie Kills");
            objKills.setDisplaySlot(DisplaySlot.PLAYER_LIST);

            Team corpseTeam = bukkitScoreboard.registerNewTeam(gameScoreboard.getZombiesArena().getCorpseTeamName());
            corpseTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            corpseTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

            // Add their in game scoreboard for every player still in the game
            Player bukkitPlayer = player.getValue().getPlayer();
            if (bukkitPlayer != null) {
                bukkitPlayer.setScoreboard(bukkitScoreboard);
            }

            for (Corpse corpse : gameScoreboard.getZombiesArena().getCorpses()) {
                corpse.addCorpseToScoreboardTeamForPlayer(player.getValue().getPlayer());
            }

            playScoreboards.put(player.getKey(), new IngamePlayerScoreboardInformation(bukkitScoreboard, this, objHealth, objKills, writer, zombieKills));
        }

        scoreboard.getZombiesArena().getPlayerJoinEvent().registerHandler(this::handleJoin);
        scoreboard.getZombiesArena().getPlayerRejoinEvent().registerHandler(this::handleRejoin);
        scoreboard.getZombiesArena().getPlayerLeaveEvent().registerHandler(this::handleLeave);
    }


    private void handleLeave(ManagingArena<ZombiesArena, ZombiesPlayer>.ManagedPlayerListArgs managedPlayerListArgs) {
        managedPlayerListArgs.getPlayers().forEach(x -> {
            Player player = x.getPlayer();
            if (player != null) {
                x.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        });
    }

    private void handleJoin(ManagingArena.PlayerListArgs playerListArgs) {
        for(var player : playerListArgs.getPlayers()) {
            if(playScoreboards.containsKey(player.getUniqueId())) {
                player.setScoreboard(playScoreboards.get(player.getUniqueId()).getBukkitScoreboard());
            } else {
                Zombies.getInstance().getLogger().log(Level.SEVERE, "Could not find scoreboard for player: " + player.getName() + " with UUID: " + player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Unable to load your scoreboard!");
            }
        }
    }

    private void handleRejoin(ZombiesArena.ManagedPlayerListArgs playerListArgs) {
        for (ZombiesPlayer player : playerListArgs.getPlayers()) {
            Player bukkitPlayer = player.getPlayer();

            if (bukkitPlayer != null) {
                if (playScoreboards.containsKey(bukkitPlayer.getUniqueId())) {
                    bukkitPlayer.setScoreboard(playScoreboards.get(bukkitPlayer.getUniqueId()).getBukkitScoreboard());
                } else {
                    Zombies.getInstance().getLogger().log(Level.SEVERE, "Could not find scoreboard for player: "
                            + bukkitPlayer.getName() + " with UUID: " + bukkitPlayer.getUniqueId());
                    bukkitPlayer.sendMessage(ChatColor.RED + "Unable to load your scoreboard!");
                }
            }
        }
    }


    @Override
    public void update() {
        // Update general information
        var playerMap =  gameScoreboard.getZombiesArena().getPlayerMap();
        zombieLeft.setValue("" + gameScoreboard.getZombiesArena().getZombiesLeft());
        if(gameScoreboard.getZombiesArena().getState() == ZombiesArenaState.ENDED) {
            round.setValue("Game Over!");
            time.setValue(formatTime(gameScoreboard.getZombiesArena().getEndTimeStamp() - gameScoreboard.getZombiesArena().getStartTimeStamp()));
        } else {
            round.setValue("Round " + (gameScoreboard.getZombiesArena().getMap().getCurrentRoundProperty().getValue(gameScoreboard.getZombiesArena()) + 1));
            time.setValue(formatTime(System.currentTimeMillis() - gameScoreboard.getZombiesArena().getStartTimeStamp()));
        }

        // Update player status
        for(var playerStatus : playerStatues.entrySet()) {
            var tfStatus = playerStatus.getValue().getRight();

            if(playerMap.containsKey(playerStatus.getKey())) {
                var player = playerMap.get(playerStatus.getKey());
                if(player == null || !player.isInGame()) {
                    tfStatus.setValue(ChatColor.RED + "QUIT");
                } else {
                    switch (player.getState()) {
                        case DEAD -> tfStatus.setValue(ChatColor.RED + "DEAD");
                        case KNOCKED -> tfStatus.setValue(ChatColor.YELLOW + "REVIVE");
                        case ALIVE -> tfStatus.setValue(ChatColor.GOLD + "" + player.getCoins());
                    }
                }
            } else {
                Zombies.getInstance().getLogger().log(Level.SEVERE, "Could not find player with UUID: " + playerStatus.getKey().toString());
                tfStatus.setValue(ChatColor.RED + "QUIT");
            }
        }

        // Update player kills and update the scoreboard
        for(var playerSb : playScoreboards.entrySet()) {
            if(playerMap.containsKey(playerSb.getKey())) {
                var player = playerMap.get(playerSb.getKey());
                playerSb.getValue().getZombieKills().setValue("" + player.getKills());
                playerSb.getValue().getSidebarWriter().update();
                gameScoreboard.getZombiesArena().getPlayerMap()
                        .forEach((l,r) -> {
                            Player otherPlayer = r.getPlayer();
                            if (otherPlayer != null) {
                                playerSb.getValue().getZombiesKillObjective().getScore(otherPlayer.getName()).setScore(r.getKills());
                                playerSb.getValue().getHealthObjective().getScore(otherPlayer.getName()).setScore((int) otherPlayer.getHealth());
                            }
                        });
            } else {
                Zombies.getInstance().getLogger().log(Level.SEVERE, "Could not find player with UUID: " + playerSb.getKey().toString());
            }
        }
    }

    private long previousMillis = 0;

    @NotNull
    private String formatTime(long timeElapsedInMillis) {
        // To avoid timer *lag* behind then *fast-forward* due to taskTimer is not precise we will round up the value
        // if the delta time is smaller than half of the interval
        long millis = timeElapsedInMillis % secondToMillis;
        long roundingMidPoint = (1000 - gameScoreboard.getRefreshRate() / 2 + previousMillis) % 1000;
        if(millis > roundingMidPoint) {
            timeElapsedInMillis += secondToMillis;
        }
        previousMillis = millis;


        var hours = timeElapsedInMillis / hourToMillis;
        var minutes = (timeElapsedInMillis % hourToMillis) / minuteToMillis;
        var seconds = (timeElapsedInMillis % minuteToMillis) / secondToMillis;

        var formatter = new DecimalFormat("00");
        StringBuilder sb = new StringBuilder();
        if(hours > 0) {
            sb.append(hours);
            sb.append(":");
        }
        sb.append(formatter.format(minutes)).append(":").append(formatter.format(seconds));
        return sb.toString();
    }

    @Override
    public void dispose() {
        gameScoreboard.getZombiesArena().getPlayerJoinEvent().removeHandler(this::handleJoin);
        gameScoreboard.getZombiesArena().getPlayerRejoinEvent().removeHandler(this::handleRejoin);
        gameScoreboard.getZombiesArena().getPlayerLeaveEvent().removeHandler(this::handleLeave);
        playScoreboards.forEach((uuid, data) -> data.getSidebarWriter().dispose());
    }
}
