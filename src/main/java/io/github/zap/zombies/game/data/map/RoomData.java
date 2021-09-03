package io.github.zap.zombies.game.data.map;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a room.
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomData {
    /**
     * The unique, non-user-friendly name of this room.
     */
    String name;

    /**
     * The message key of the room name
     */
    String roomDisplayName;

    /**
     * The bounds of this room, used for knockdown messages and other things
     */
    MultiBoundingBox bounds = new MultiBoundingBox();

    /**
     * All of the windows contained in this room
     */
    List<WindowData> windows = new ArrayList<>();

    /**
     * All of the spawnpoints contained in this room
     */
    List<SpawnpointData> spawnpoints = new ArrayList<>();

    /**
     * Whether or not this room is the 'spawn' room; where the players start off in
     */
    boolean isSpawn = false;

    /**
     * Arena specific state: whether or not this room has been opened.
     */
    transient final Property<Boolean> openProperty = new Property<>(false);

    private RoomData() {}

    public RoomData(String name) {
        this.name = name;
        this.roomDisplayName = name;
    }
}
