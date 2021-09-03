package io.github.zap.zombies.game.data.map;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.Unique;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.sound.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a window.
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WindowData {
    /**
     * The materials that should be used to repair this window. Each index corresponds to the coordinate located at
     * the same index in faceVectors.
     */
    List<String> repairedData = new ArrayList<>();

    /**
     * A list of vectors corresponding to the blocks of window face
     */
    List<Vector> faceVectors = new ArrayList<>();

    /**
     * A BoundingBox containing the face of the window
     */
    BoundingBox faceBounds;

    /**
     * The bounds of the window interior - used for player position checking and entity AI
     */
    MultiBoundingBox interiorBounds = new MultiBoundingBox();

    /**
     * The coordinate considered the 'base' of the window, to which players are teleported if they enter the interior
     * It is also the location that mobs navigate to when they leave the window
     */
    Vector target;

    /**
     * The spawnpoints held in this window
     */
    List<SpawnpointData> spawnpoints = new ArrayList<>();

    /**
     * The sound that is played when a single block from the window breaks
     */
    Sound blockBreakSound = Sound.sound(org.bukkit.Sound.BLOCK_WOOD_BREAK.getKey(), Sound.Source.HOSTILE, 3F, 0.8F);

    /**
     * The sound that plays when the window is entirely broken
     */
    Sound windowBreakSound = Sound.sound(org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR.getKey(), Sound.Source.HOSTILE, 3F, 1F);

    /**
     * The sound that plays when a single block is repaired
     */
    Sound blockRepairSound = Sound.sound(org.bukkit.Sound.BLOCK_WOOD_PLACE.getKey(), Sound.Source.PLAYER, 3F, 1F);

    /**
     * The sound that plays when the entire window has been repaired
     */
    Sound windowRepairSound = Sound.sound(org.bukkit.Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE.getKey(), Sound.Source.PLAYER, 3F, 1F);

    /**
     * Arena specific state: the current index at which the window is being repaired or broken. This points to the index
     * of the current repaired block; thus, if the window is fully broken, it will == -1
     *
     * The default value is calculated as soon as it is needed
     */
    transient final Property<Integer> currentIndexProperty = new Property<>(() -> getVolume() - 1);

    /**
     * Arena specific state: the player who is currently repairing the window
     */
    transient final Property<ZombiesPlayer> repairingPlayerProperty = new Property<>((ZombiesPlayer) null);

    /**
     * Arena specific state: the entity that is currently attacking the window
     */
    transient final Property<Entity> attackingEntityProperty = new Property<>((Entity) null);

    private WindowData() { }

    public WindowData(World from, BoundingBox faceBounds, Vector target) {
        this.faceBounds = faceBounds;

        Vector min = faceBounds.getMin();
        Vector max = faceBounds.getMax();

        for(int x = min.getBlockX(); x < max.getBlockX(); x++) {
            for(int y = min.getBlockY(); y < max.getBlockY(); y++) {
                for(int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                    Block block = from.getBlockAt(x, y, z);
                    repairedData.add(block.getBlockData().getAsString());
                    faceVectors.add(new Vector(x, y, z));
                }
            }
        }

        this.target = target;
    }

    /**
     * Gets the center of the window's face (its breakable/repairable blocks)
     * @return The central vector of the window's face
     */
    public Vector getCenter() {
        return faceBounds.getCenter();
    }

    /**
     * Gets the volume of the window's face (its breakable/repairable blocks)
     * @return The volume of the window's face
     */
    public int getVolume() {
        return (int)faceBounds.getVolume();
    }

    /**
     * Performs a range check on the window.
     * @param standing The vector to base the check from
     * @param distanceSquared The distance to base the range check off of
     * @return Whether or not the window is within the specified distance from the standing vector
     */
    public boolean inRange(Vector standing, double distanceSquared) {
        return standing.distanceSquared(getCenter()) < distanceSquared;
    }

    /**
     * Incrementally repairs this window by the specified amount. The repair index is limited by the volume of the
     * window.
     * @param accessor The accessor using this object
     * @param by The amount to try to advance the repair index by
     * @return The number of blocks that were actually repaired
     */
    public int advanceRepairState(Unique accessor, int by) {
        int currentIndex = currentIndexProperty.getValue(accessor);
        int max = getVolume() - 1;

        if(currentIndex < max) {
            int newValue = Math.min(currentIndex + by, max);
            int repaired = newValue - currentIndex;

            currentIndexProperty.setValue(accessor, newValue);
            return repaired;
        }

        return 0;
    }

    /**
     * Incrementally breaks the window by the specified amount. Works the same as advanceRepairState, but in reverse.
     * @param accessor The accessor using this object
     * @param by The amount to reduce the repair index by
     * @return true if any number of breaks occurred, false otherwise
     */
    public int retractRepairState(Unique accessor, int by) {
        int currentIndex = currentIndexProperty.getValue(accessor);

        if(currentIndex > -1) {
            int newValue = Math.max(-1, currentIndex - by);
            int broken = currentIndex - newValue;

            currentIndexProperty.setValue(accessor, newValue);
            return broken;
        }

        return 0;
    }

    public boolean isFullyRepaired(Unique accessor) {
        return currentIndexProperty.getValue(accessor) == getVolume() - 1;
    }

    public boolean playerInside(Vector location) {
        return getInteriorBounds().contains(location) || faceBounds.clone().expand(0.3).expandDirectional(0, -1, 0).contains(location);
    }
}
