package io.github.zap.zombies;

import io.github.zap.arenaapi.Disposable;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class used to render visuals based on a player loading a chunk
 */
public class ChunkLoadHandler implements Disposable, Listener {

    private final Map<World, Map<Long, List<Consumer<Player>>>> consumerMap = new HashMap<>();

    public ChunkLoadHandler() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Zombies.getInstance());
    }

    /**
     * Adds a consumer to be called when the chunk in the location is laoded by a player
     * @param location The location to get the chunk from
     * @param consumer The callback to execute
     */
    public void addConsumer(Location location, Consumer<Player> consumer) {
        Map<Long, List<Consumer<Player>>> chunkMap =
                consumerMap.computeIfAbsent(location.getWorld(), (chunk) -> new HashMap<>());
        List<Consumer<Player>> consumers = chunkMap
                .computeIfAbsent(location.getChunk().getChunkKey(), (chunkKey) -> new ArrayList<>());

        consumers.add(consumer);
    }

    @EventHandler
    private void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        Map<Long, List<Consumer<Player>>> chunkMap = consumerMap.get(event.getWorld());
        if (chunkMap != null) {
            List<Consumer<Player>> consumers = chunkMap.get(event.getChunk().getChunkKey());

            if (consumers != null) {
                for (Consumer<Player> consumer : consumers) {
                    consumer.accept(event.getPlayer());
                }
            }
        }
    }

    @Override
    public void dispose() {
        HandlerList.unregisterAll(this);
    }

}
