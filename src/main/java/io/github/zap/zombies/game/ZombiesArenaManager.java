package io.github.zap.zombies.game;

import io.github.zap.arenaapi.LoadFailureException;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.arenaapi.stats.FileStatsManager;
import io.github.zap.arenaapi.stats.StatsCache;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.zapcommons.shadow.com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.equipment.JacksonEquipmentManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.shop.JacksonShopManager;
import io.github.zap.zombies.game.data.map.shop.ShopManager;
import io.github.zap.zombies.game.powerups.managers.JacksonPowerUpManager;
import io.github.zap.zombies.game.powerups.managers.JacksonPowerUpManagerOptions;
import io.github.zap.zombies.game.powerups.managers.PowerUpManager;
import io.github.zap.zombies.leaderboard.BasicLeaderboardEntrySource;
import io.github.zap.zombies.leaderboard.BasicLeaderboardLineSource;
import io.github.zap.zombies.leaderboard.HologramLeaderboardView;
import io.github.zap.zombies.leaderboard.Leaderboard;
import io.github.zap.zombies.leaderboard.LeaderboardEntry;
import io.github.zap.zombies.leaderboard.LeaderboardEntrySource;
import io.github.zap.zombies.leaderboard.LeaderboardLineCreator;
import io.github.zap.zombies.leaderboard.LeaderboardView;
import io.github.zap.zombies.leaderboard.times.TimesFormatter;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.map.MapStats;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * This class manages active arenas and loads new ones as required, up to a specified limit. It is also responsible for
 * routing players to other servers if the capacity is reached.
 */
public class ZombiesArenaManager extends ArenaManager<ZombiesArena> {
    private static final String NAME = "zombies";

    @Getter
    private final EquipmentManager equipmentManager;

    @Getter
    private final PowerUpManager powerUpManager;

    @Getter
    private final ShopManager shopManager;

    @Getter
    private final int arenaCapacity;

    @Getter
    private final int arenaTimeout;

    @Getter
    private final DataLoader mapLoader;

    private final Map<String, MapData> maps = new HashMap<>();

    private final Set<String> markedForDeletion = new HashSet<>();

    public ZombiesArenaManager(Location hubLocation, DataLoader mapLoader, DataLoader equipmentLoader,
                               DataLoader powerUpLoader, DataLoader playerStatsLoader, DataLoader mapStatsLoader,
                               int arenaCapacity, int arenaTimeout) {
        super(NAME, hubLocation, createStatsManager(playerStatsLoader, mapStatsLoader));
        this.equipmentManager = new JacksonEquipmentManager(equipmentLoader);
        this.powerUpManager = new JacksonPowerUpManager(powerUpLoader, new JacksonPowerUpManagerOptions());
        ((JacksonPowerUpManager) this.powerUpManager).load();
        this.shopManager = new JacksonShopManager();
        this.arenaCapacity = arenaCapacity;
        this.arenaTimeout = arenaTimeout;
        this.mapLoader = mapLoader;

    }

    private static StatsManager createStatsManager(DataLoader playerStatsLoader, DataLoader mapStatsLoader) {
        StatsManager statsManager = new FileStatsManager(Zombies.getInstance(),
                Executors.newFixedThreadPool(1),
                Map.of(CacheInformation.PLAYER, playerStatsLoader, CacheInformation.MAP, mapStatsLoader));
        statsManager.registerCache(new StatsCache<>(Zombies.getInstance(), CacheInformation.PLAYER,
                PlayerGeneralStats.class, CacheInformation.MAX_FREE_MAP_CACHE_SIZE));
        statsManager.registerCache(new StatsCache<>(Zombies.getInstance(), CacheInformation.MAP, MapStats.class,
                CacheInformation.MAX_FREE_MAP_CACHE_SIZE));

        return statsManager;
    }

    @Override
    public String getGameName() {
        return NAME;
    }

    public void handleJoin(JoinInformation information, Consumer<Pair<Boolean, String>> onCompletion) {
        if(!information.getJoinable().validate()) {
            onCompletion.accept(Pair.of(false, "Someone is offline and therefore unable to join!"));
            return;
        }

        String mapName = information.getMapName();
        UUID targetArena = information.getTargetArena();

        if(mapName != null) {
            MapData mapData = maps.get(mapName);

            if(mapData != null) {
                for(ZombiesArena arena : managedArenas.values()) {
                    if(arena.getMap().getName().equals(mapName) && arena.handleJoin(information.getJoinable().getPlayers())) {
                        onCompletion.accept(Pair.of(true, null));
                        return;
                    }
                }

                if(managedArenas.size() < arenaCapacity) {
                    Zombies.info(String.format("Loading arena for map '%s'.", mapName));
                    Zombies.info(String.format("JoinInformation that triggered this load: '%s'.", information));

                    CompletableFuture<World> worldFuture = Zombies.getInstance().getWorldLoader().loadWorld(mapData.getWorldName());
                    try {
                        World world = worldFuture.get();
                        world.setGameRule(GameRule.DO_FIRE_TICK, false);
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                        world.setGameRule(GameRule.DO_INSOMNIA, false);
                        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
                        world.setGameRule(GameRule.MOB_GRIEFING, false);
                        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
                        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
                        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
                        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);

                        world.setTime(mapData.getWorldTime());

                        statsManager.queueCacheRequest(CacheInformation.MAP, mapData.getName(), MapStats::new,
                                (stats) -> {
                            ConcurrentMap<UUID, Component> playerNames = new ConcurrentHashMap<>();

                            List<Map.Entry<UUID, Long>> bestTimes = new ArrayList<>(stats.getBestTimes().entrySet());
                            bestTimes.sort(Comparator.comparingLong(Map.Entry::getValue));
                            int bestTimesCount = Math.min(mapData.getBestTimesCount(), bestTimes.size());

                            List<LeaderboardEntry> entries = new ArrayList<>();
                            LeaderboardEntrySource entrySource = new BasicLeaderboardEntrySource(entries);
                            BasicLeaderboardLineSource lineSource = new BasicLeaderboardLineSource(entrySource,
                                    LeaderboardLineCreator.defaultCreator());

                            Vector delta = new Vector(0,
                                    Hologram.DEFAULT_LINE_SPACE * bestTimesCount, 0);
                            Vector hologramLocation = mapData
                                    .getBestTimesLocation()
                                    .clone()
                                    .add(delta);

                            // December 2021 event
                            List<Map.Entry<UUID, Long>> bestTimes2 = new ArrayList<>(stats.getDecember2021Event().entrySet());
                            bestTimes2.sort(Comparator.comparingLong(Map.Entry::getValue));
                            int bestTimesCount2 = Math.min(mapData.getBestTimesCount(), bestTimes2.size());

                            List<LeaderboardEntry> entries2 = new ArrayList<>();
                            LeaderboardEntrySource entrySource2 = new BasicLeaderboardEntrySource(entries2);
                            BasicLeaderboardLineSource lineSource2 = new BasicLeaderboardLineSource(entrySource2,
                                    LeaderboardLineCreator.defaultCreator());

                            Vector delta2 = new Vector(0,
                                    Hologram.DEFAULT_LINE_SPACE * bestTimesCount2, 0);
                            Vector hologramLocation2 = mapData
                                    .getDecember2021BestTimesLocation()
                                    .clone()
                                    .add(delta2);

                            CompletableFuture<Leaderboard> future = new CompletableFuture<>();
                            // December 2021 event
                            CompletableFuture<Leaderboard> future2 = new CompletableFuture<>();
                            Bukkit.getServer().getScheduler().runTask(Zombies.getInstance(), () -> {
                                Hologram hologram = new Hologram(hologramLocation.toLocation(world));
                                hologram.addLine(Component.text("Best Times", NamedTextColor.BLUE));

                                // December 2021 event
                                Hologram hologram2 = new Hologram(hologramLocation2.toLocation(world));
                                hologram2.addLine(Component.text("December 2021 Event", NamedTextColor.GOLD));

                                TimesFormatter formatter = TimesFormatter.defaultFormatter();
                                for (int i = 0; i < bestTimesCount; i++) {
                                    Map.Entry<UUID, Long> bestTime = bestTimes.get(i);
                                    Component time = formatter.format(bestTime.getValue());

                                    LeaderboardEntry entry = new LeaderboardEntry() {
                                        @Override
                                        public @NotNull Component player() {
                                            return playerNames.getOrDefault(bestTime.getKey(),
                                                    Component.text("Loading...", NamedTextColor.GRAY));
                                        }

                                        @Override
                                        public @NotNull Component value() {
                                            return time;
                                        }
                                    };

                                    entries.add(entry);
                                    hologram.addLine(lineSource.getEntry(i));
                                }
                                // December 2021 event
                                for (int i = 0; i < bestTimesCount2; i++) {
                                    Map.Entry<UUID, Long> bestTime = bestTimes2.get(i);
                                    Component time = formatter.format(bestTime.getValue());

                                    LeaderboardEntry entry = new LeaderboardEntry() {
                                        @Override
                                        public @NotNull Component player() {
                                            return playerNames.getOrDefault(bestTime.getKey(),
                                                    Component.text("Loading...", NamedTextColor.GRAY));
                                        }

                                        @Override
                                        public @NotNull Component value() {
                                            return time;
                                        }
                                    };

                                    entries2.add(entry);
                                    hologram2.addLine(lineSource2.getEntry(i));
                                }

                                LeaderboardView view = new HologramLeaderboardView(hologram, 1);

                                Leaderboard leaderboard = new Leaderboard(lineSource, view);
                                leaderboard.updateAll();

                                future.complete(leaderboard);

                                // December 2021 event
                                LeaderboardView view2 = new HologramLeaderboardView(hologram2, 1);

                                Leaderboard leaderboard2 = new Leaderboard(lineSource2, view2);
                                leaderboard2.updateAll();

                                future2.complete(leaderboard);

                                ZombiesArena arena = new ZombiesArena(this, world, maps.get(mapName),
                                        leaderboard, leaderboard2, arenaTimeout);
                                managedArenas.put(arena.getId(), arena);

                                getArenaCreated().callEvent(arena);
                                if (arena.handleJoin(information.getJoinable().getPlayers())) {
                                    onCompletion.accept(Pair.of(true, null));
                                }
                                else {
                                    Zombies.warning(String.format("Newly created arena rejected join request '%s'.",
                                            information));
                                    onCompletion.accept(Pair.of(false, "Tried to make a new arena," +
                                            " but it couldn't accept all of the players!"));
                                }
                            });

                            future.whenCompleteAsync((leaderboard, throwable) -> {
                                if (throwable != null) {
                                    Zombies.getInstance().getLogger().log(Level.WARNING, "Error while creating " +
                                            "times leaderboard", throwable);
                                }
                                else {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    for (int i = 0; i < bestTimesCount; i++) {
                                        UUID uuid = bestTimes.get(i).getKey();

                                        try {
                                            URL url = new URL("https://sessionserver.mojang.com/" +
                                                    "session/minecraft/profile/" + uuid);
                                            String message = IOUtils.toString(url, Charset.defaultCharset());
                                            String name = objectMapper.readTree(message).get("name").textValue();

                                            playerNames.put(uuid, Component.text(name, NamedTextColor.GRAY));
                                            leaderboard.update(i);
                                        } catch (IOException e) {
                                            Zombies.warning("Failed to get name of player with UUID " + uuid);
                                        }
                                    }
                                }
                            });

                            // December 2021 event
                            future2.whenCompleteAsync((leaderboard, throwable) -> {
                                if (throwable != null) {
                                    Zombies.getInstance().getLogger().log(Level.WARNING, "Error while creating " +
                                            "times leaderboard", throwable);
                                }
                                else {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    for (int i = 0; i < bestTimesCount2; i++) {
                                        UUID uuid = bestTimes2.get(i).getKey();

                                        try {
                                            URL url = new URL("https://sessionserver.mojang.com/" +
                                                    "session/minecraft/profile/" + uuid);
                                            String message = IOUtils.toString(url, Charset.defaultCharset());
                                            String name = objectMapper.readTree(message).get("name").textValue();

                                            playerNames.put(uuid, Component.text(name, NamedTextColor.GRAY));
                                            leaderboard.update(i);
                                        } catch (IOException e) {
                                            Zombies.warning("Failed to get name of player with UUID " + uuid);
                                        }
                                    }
                                }
                            });
                        });
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    return;
                }
                else {
                    Zombies.info("A JoinAttempt was rejected, as we have reached arena capacity.");
                }
            }
            else {
                Zombies.warning(String.format("A map named '%s' does not exist.", mapName));
            }
        }
        else {
            ZombiesArena arena = managedArenas.get(targetArena);

            if(arena != null) {
                if(arena.handleJoin(information.getJoinable().getPlayers())) {
                    onCompletion.accept(Pair.of(true, null));
                }
                else {
                    onCompletion.accept(Pair.of(false, "The arena rejected the join request."));
                }

                return;
            }
            else {
                Zombies.warning(String.format("Specific requested arena '%s' does not exist.", targetArena));
            }
        }

        onCompletion.accept(Pair.of(false, "An unknown error occurred."));
    }

    @Override
    public boolean acceptsPlayers() {
        return true;
    }

    @Override
    public void unloadArena(ZombiesArena arena) {
        for(Player player : arena.getWorld().getPlayers()) { //tp out any players that could prevent us from unloading
            player.teleport(getHubLocation());
        }

        managedArenas.remove(arena.getId());

        //we are doing a single-world, single-arena approach so no need to check for other arenas sharing this world
        Zombies.getInstance().getWorldLoader().unloadWorld(arena.getWorld());
    }

    @Override
    public boolean hasMap(String mapName) {
        return maps.containsKey(mapName);
    }

    public MapData getMap(String name) {
        return maps.get(name);
    }

    public void addMap(MapData data) {
        String name = data.getName();

        if(maps.containsKey(name)) {
            throw new UnsupportedOperationException("cannot add a map that already exists");
        }

        maps.put(name, data);
    }

    public void removeMap(String name) {
        maps.remove(name);
    }

    public List<MapData> getMaps() {
        return new ArrayList<>(maps.values());
    }

    public void deleteOnDisable(String mapName) {
        if(maps.containsKey(mapName)) {
            markedForDeletion.add(mapName);
        }
    }

    public boolean canDelete(String mapName) {
        return markedForDeletion.contains(mapName);
    }

    public void loadMaps() throws LoadFailureException {
        Zombies.info("Loading maps. Changes will not apply to existing map data.");

        File[] files = mapLoader.getRootDirectory().listFiles();
        if(files != null) {
            Zombies.info(String.format("Found %s file(s) in the map directory.", files.length));

            for(File file : files) {
                MapData map = this.mapLoader.load(FilenameUtils.getBaseName(file.getName()), MapData.class);

                if(map != null) {
                    maps.put(map.getName(), map);
                    Zombies.info(String.format("Loaded MapData for '%s'", map.getName()));
                }
                else {
                    throw new LoadFailureException("Unable to properly load some of the provided map data.");
                }
            }
        }
    }
}
