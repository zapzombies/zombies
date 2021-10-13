package io.github.zap.zombies.world;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import io.github.zap.arenaapi.world.WorldLoader;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Class used to load a Zombies map with the Slime format
 */
public class SlimeWorldLoader implements WorldLoader {
    private final Plugin plugin;
    private final SlimeLoader slimeLoader;
    private final SlimePlugin slimePlugin;

    /**
     * Creates a new instance of SlimeMapLoader given a SlimePlugin.
     * @param slimeLoader The SlimeLoader that this instance uses
     */
    public SlimeWorldLoader(@NotNull Plugin plugin, @NotNull SlimeLoader slimeLoader, @NotNull SlimePlugin slimePlugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.slimeLoader = Objects.requireNonNull(slimeLoader, "slimeLoader cannot be null");
        this.slimePlugin = Objects.requireNonNull(slimePlugin, "slimePlugin cannot be null");
    }

    @Override
    public void preload() {}

    @Override
    public @NotNull CompletableFuture<World> loadWorld(String worldName) {
        try {
            String randomName = UUID.randomUUID().toString();
            slimePlugin.generateWorld(slimePlugin.loadWorld(slimeLoader, worldName, true,
                    new SlimePropertyMap()).clone(randomName));
            return CompletableFuture.completedFuture(Bukkit.getWorld(randomName));
        } catch (IOException | UnknownWorldException | CorruptedWorldException | NewerFormatException |
                WorldInUseException e) {
            plugin.getLogger().log(Level.WARNING, "World " + worldName + " could not be loaded", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean unloadWorld(World world) {
        if(!Bukkit.unloadWorld(world, false)) {
            plugin.getLogger().warning("Failed to unload world " + world);
            return false;
        }

        return true;
    }

    @Override
    public boolean worldExists(String worldName) {
        try {
            return slimeLoader.worldExists(worldName);
        }
        catch(IOException e) {
            plugin.getLogger().log(Level.WARNING, "Exception when querying worlds", e);
        }

        return false;
    }
}
