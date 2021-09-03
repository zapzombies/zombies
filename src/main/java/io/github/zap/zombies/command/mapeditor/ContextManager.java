package io.github.zap.zombies.command.mapeditor;

import com.google.common.collect.Lists;
import io.github.zap.zombies.Zombies;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ContextManager implements Listener {
    private static final Material itemType = Material.STICK;
    private static final List<String> itemLore = Lists.newArrayList("Zombies Map Editor Wand[TM]");

    private final ItemStack editorItem = new ItemStack(itemType);
    private final Map<UUID, EditorContext> contextMap = new HashMap<>();

    public ContextManager() {
        Zombies zombies = Zombies.getInstance();
        zombies.getServer().getPluginManager().registerEvents(this, zombies);
        editorItem.setLore(itemLore);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            ItemStack item = event.getItem();
            Block block = event.getClickedBlock();
            if(item != null && block != null && item.getType() == itemType) {
                Player player = event.getPlayer();
                EditorContext context = contextMap.computeIfAbsent(event.getPlayer().getUniqueId(), uuid -> new EditorContext(player));

                if(itemLore.equals(item.getLore())) {
                    context.handleClicked(block);
                }
            }
        }
    }

    public ItemStack getEditorItem() {
        return editorItem.clone();
    }

    public EditorContext getContext(Player player) {
        return contextMap.computeIfAbsent(player.getUniqueId(), mapping -> new EditorContext(player));
    }

    public boolean hasContext(Player player) {
        return contextMap.containsKey(player.getUniqueId());
    }

    public void removeContext(Player player) {
        contextMap.remove(player.getUniqueId());
    }

    public Collection<EditorContext> getContexts() {
        return contextMap.values();
    }
}
