package io.github.zap.zombies.game.data.map;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;

/**
 * This class represents a Zombies map. It is effectively a pure data class; it only contains helper functions for
 * retrieving and manipulating its own data values.
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MapData {
    /**
     * The unique name of this map that need not be user friendly
     */
    String name;

    /**
     * The display name of this map
     */
    String mapDisplayName;

    /**
     * The name of the world corresponding to this map
     */
    String worldName;

    /**
     * The material used in the item stack representation of the map
     */
    Material itemStackMaterial;

    /**
     * The display name of the item stack representation of the map
     */
    String itemStackDisplayName;

    /**
     * The lore associated with the item stack representation of the map
     */
    List<String> itemStackLore;

    /**
     * The time the world should be set at.
     */
    long worldTime;

    /**
     * The bounds of the map, inside which every component should exist
     */
    BoundingBox mapBounds = new BoundingBox();

    /**
     * Subtitles to display when the player joins a Zombies game and a title is sent to them
     */
    List<String> splashScreenSubtitles = new ArrayList<>();

    /**
     * The spawn vector of this map.
     */
    Vector spawn = new Vector();

    /**
     * The vector to the bottom of where records should be displayed
     */
    Vector bestTimesLocation = new Vector();

    /**
     * The number of best times to display in the pregame
     */
    int bestTimesCount = 12;

    /**
     * The minimum required number of players that this map can start with
     */
    int minimumCapacity = 4;

    /**
     * The maximum number of players this map can hold
     */
    int maximumCapacity = 4;

    /**
     * The duration of the game start countdown timer, in seconds
     */
    int countdownSeconds = 20;

    /**
     * The number of coins each player should start with
     */
    int startingCoins = 0;

    /**
     * The number of coins you get for repairing a window
     */
    int coinsOnRepair = 20;

    /**
     * Whether or not spectators are allowed here
     */
    boolean spectatorAllowed = true;

    /**
     * If this is true, players will be required to be holding nothing in order to open doors
     */
    boolean handRequiredToOpenDoors = false;

    /**
     * Whether or not the players should be allowed to forcibly start the game regardless of the minimum player limit
     */
    boolean forceStart = false;

    /**
     * Whether this map allows players to rejoin after the game has started
     */
    boolean allowRejoin = true;

    /**
     * The minimum (Manhattan) distance in blocks that players must be from a window in order to repair it
     */
    int windowRepairRadiusSquared = 16;

    /**
     * The base delay, in Minecraft server ticks (20ths of a second) that occurs between window blocks being repaired
     */
    int windowRepairTicks = 20;

    /**
     * The time it takes, in Minecraft server ticks, for a corpse to die and for players to no longer be able to revive
     * the corpse
     */
    int corpseDeathTime = 500;

    /**
     * The minimum distance in blocks that players must be from a player corpse in order to revive it
     */
    int reviveRadius = 3;

    /**
     * Whether or not to allow "wallshooting", allowing players to shoot mobs through walls if their shot passes through
     * a non-solid block
     */
    boolean allowWallshooting = false;

    /**
     * The MythicMobs mob level that mobs will spawn at
     */
    int mobSpawnLevel = 1;

    /**
     * The material that should replace door blocks when they are opened.
     */
    Material doorFillMaterial = Material.AIR;

    //perk stuff below

    /**
     * Whether or not perks should be lost when a player dies.
     */
    boolean perksLostOnDeath = true;

    /**
     * Whether or not perks should be lost when a player quits the game.
     */
    boolean perksLostOnQuit = true;

    /**
     * The default number of ticks it takes for a player to be revived
     */
    int defaultReviveTime = 30;

    /**
     * Number of rolls before a chest moves to a new location
     */
    int rollsPerChest = 5;

    /**
     * Whether the lucky chest can have its initial location in the spawn room
     */
    boolean chestCanStartInSpawnRoom = false;

    /**
     * The level of speed that you get when you open a door
     */
    int doorSpeedLevel = 1;

    /**
     * The number of ticks of speed you get when you open a door
     */
    int doorSpeedTicks = 100;

    /**
     * The level of speed that you get when you revive of player
     */
    int reviveSpeedLevel = 1;

    /**
     * The number of ticks of speed you get when you revive a player
     */
    int reviveSpeedTicks = 100;

    /**
     * The rounds that should have times saved for players
     */
    Set<Integer> roundTimesShouldSave = new HashSet<>();

    /**
     * Equipments given at the start of the game by their name
     */
    List<String> defaultEquipments = new ArrayList<>();

    /**
     * A map of SpawnRule objects which are used to define the behavior of spawnpoints.
     */
    Map<String, SpawnRule> spawnRules = new HashMap<>();

    /**
     * Map of hotbar object group names to the slots allocated for them
     */
    Map<String, Set<Integer>> hotbarObjectGroupSlots = new HashMap<>();

    /**
     * Define conditions to spawn power up
     * Left String are SpawnRule data
     * Right String are PowerUp names
     */
    Set<Pair<String, String>> powerUpSpawnRules = new HashSet<>();

    /**
     * Rounds that cannot spawn powerups.
     */
    Set<Integer> disablePowerUpRound = new HashSet<>();

    /**
     * All the rounds in the game
     */
    List<RoundData> rounds = new ArrayList<>();

    /**
     * The list of rooms managed by this map
     */
    List<RoomData> rooms = new ArrayList<>();

    /**
     * All doors in this map
     */
    List<DoorData> doors = new ArrayList<>();

    /**
     * All the shops managed by this map
     */
    List<ShopData> shops = new ArrayList<>();

    transient final Property<Integer> currentRoundProperty = new Property<>(0);

    @SuppressWarnings("unused")
    private MapData() {}

    public MapData(String mapName, String worldName, BoundingBox bounds) {
        this.name = mapName;
        this.worldName = worldName;
        this.mapDisplayName = mapName;
        this.mapBounds = bounds;
    }

    /**
     * Gets the window whose face contains the provided vector, or null if the vector is not inside any windows.
     * @param target The vector that may be inside of a window
     * @return The window the vector is in, or null if it's not inside anything
     */
    public WindowData windowAt(Vector target) {
        if(mapBounds.contains(target)) {
            for(RoomData roomData : rooms) {
                if(roomData.getBounds().contains(target)) { //optimization: don't iterate through rooms we don't need to
                    for(WindowData window : roomData.getWindows()) {
                        if(window.getFaceBounds().contains(target)) {
                            return window;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Searches for a window that matches the given predicate. Will return null if no matching window exists.
     * @param predicate The predicate to test windows for
     * @return The first window matching the predicate, or null if none could be found
     */
    public WindowData windowMatching(Predicate<WindowData> predicate) {
        for(RoomData roomData : rooms) {
            for(WindowData window : roomData.getWindows()) {
                if(predicate.test(window)) {
                    return window;
                }
            }
        }

        return null;
    }

    /**
     * Gets the room whose bounds contains the specified vector.
     * @param target The vector to search rooms for
     * @return The room whose bounds contains the specified vector, or null
     */
    public RoomData roomAt(Vector target) {
        if(mapBounds.contains(target)) {
            for(RoomData room : rooms) {
                if(room.getBounds().contains(target)) {
                    return room;
                }
            }
        }

        return null;
    }

    public RoomData getNamedRoom(String roomName) {
        for(RoomData room : rooms) {
            if(room.getName().equals(roomName)) {
                return room;
            }
        }

        return null;
    }
}
