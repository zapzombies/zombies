package io.github.zap.zombies.game.util;

import io.github.zap.zombies.game.ZombiesArena;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

/**
 * Utility class to play sets of notes
 */
public class Jingle {

    /**
     * Plays a set of notes
     * @param zombiesArena The zombies arena the jingle is played in
     * @param jingle The notes to play
     * @param location The location to play the jingle at
     */
    public static void play(ZombiesArena zombiesArena, List<Note> jingle, JingleListener jingleListener,
                            Location location) {
        play(zombiesArena, jingle, jingleListener, location, 0);
    }

    /**
     * Plays the soundNumberth note of the jingle
     * @param zombiesArena The zombies arena the jingle is played in
     * @param location The location to play the note at
     * @param soundNumber The note number in the jingle
     */
    public static void play(ZombiesArena zombiesArena, List<Note> jingle, JingleListener jingleListener,
                            Location location, int soundNumber) {
        if (soundNumber < jingle.size()) {
            if (soundNumber == 0) {
                jingleListener.onStart(jingle);
            }

            World world = location.getWorld();
            Note note = jingle.get(soundNumber);

            zombiesArena.runTaskLater(note.getLength(), () -> {
                for (Sound sound : note.getSounds()) {
                    world.playSound(sound, location.getX(), location.getY(), location.getZ());
                }
                jingleListener.onNotePlayed(jingle);

                play(zombiesArena, jingle, jingleListener, location, soundNumber + 1);
            });
        } else {
            jingleListener.onEnd(jingle);
        }
    }

    @Getter
    public static class Note {
        List<Sound> sounds;
        long length;

        public Note() {

        }

    }

    /**
     * Listener for parts of a jingle being played
     */
    public interface JingleListener {

        /**
         * Method called when the jingle playing begins
         */
        default void onStart(List<Note> jingle) {

        }

        /**
         * Method called when a note of the jingle is played
         */
        default void onNotePlayed(List<Note> jingle) {

        }

        /**
         * Method called upon jingle completion
         */
        default void onEnd(List<Note> jingle) {

        }
    }

}
