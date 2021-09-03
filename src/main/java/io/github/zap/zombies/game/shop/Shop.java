package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Represents a station at which items are purchasable
 * @param <D> The data type of the shop
 */
@Getter
public abstract class Shop<D extends ShopData> {

    private final ZombiesArena arena;
    private final D shopData;
    private boolean powered = false;

    public Shop(ZombiesArena arena, D shopData) {
        this.arena = arena;
        this.shopData = shopData;

        registerArenaEvents();
    }

    /**
     * Registers all events from the zombie arena that will be monitored by the shop
     */
    protected void registerArenaEvents() {
        arena.getShopEvent(ShopType.POWER_SWITCH.name()).registerHandler(args -> {
            powered = true;
            display();
        });

        arena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        arena.getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);
    }

    /**
     * Called when players join the arena
     * @param args The list of players
     */
    protected void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        for (Player player : args.getPlayers()) {
            displayToPlayer(player);
        }
    }

    /**
     * Called when players rejoin the arena
     * @param args The list of players
     */
    protected void onPlayerRejoin(ZombiesArena.ManagedPlayerListArgs args) {
        for (ZombiesPlayer player : args.getPlayers()) {
            Player bukkitPlayer = player.getPlayer();

            if (bukkitPlayer != null) {
                displayToPlayer(bukkitPlayer);
            }
        }
    }

    /**
     * Method to call when a player purchases an item
     * @param zombiesPlayer The purchasing player
     */
    protected void onPurchaseSuccess(ZombiesPlayer zombiesPlayer) {
        arena.getShopEvent(getShopType()).callEvent(new ShopEventArgs(this, zombiesPlayer));
    }

    /**
     * Displays the shop to all players in its current state
     */
    public void display() {
        for (Player player : arena.getWorld().getPlayers()) {
            displayToPlayer(player);
        }
    }

    /**
     * Displays the shop to a single player
     * @param player THe player to display the shop to
     */
    protected void displayToPlayer(Player player) {

    }

    /**
     * Attempts to purchase an item for a player
     * @param args The event called that could cause a shop's interaction
     * @return Whether an interaction occurred
     */
    public abstract boolean interact(ZombiesArena.ProxyArgs<? extends Event> args);

    /**
     * Gets the type of the shop
     * @return A representation of the type of the shop
     */
    public abstract String getShopType();
}
