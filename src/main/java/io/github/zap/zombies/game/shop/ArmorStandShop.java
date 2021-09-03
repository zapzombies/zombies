package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.ArmorStandShopData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

/**
 * Shop which interacts with a single invisible armor stand
 * @param <D> The data type of the shop
 */
@Getter
public abstract class ArmorStandShop<D extends ArmorStandShopData> extends Shop<D> {

    private final Hologram hologram;

    private final ArmorStand armorStand;

    public ArmorStandShop(ZombiesArena zombiesArena, D shopData) {
        super(zombiesArena, shopData);

        World world = zombiesArena.getWorld();

        armorStand = world.spawn(
                getShopData().getRootLocation().toLocation(world).add(0.5D, -1.0D, 0.5D),
                ArmorStand.class
        );
        armorStand.setCollidable(false);
        armorStand.setGravity(false);
        armorStand.setVisible(false);

        hologram = new Hologram(getShopData().getRootLocation().toLocation(world).add(0.5D, -2.0D, 0.5D));
    }

    @Override
    public void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        Hologram hologram = getHologram();
        for (Player player : args.getPlayers()) {
            hologram.renderToPlayer(player);
        }

        super.onPlayerJoin(args);
    }

    @Override
    public void onPlayerRejoin(ZombiesArena.ManagedPlayerListArgs args) {
        Hologram hologram = getHologram();

        for (ZombiesPlayer player : args.getPlayers()) {
            Player bukkitPlayer = player.getPlayer();

            if (bukkitPlayer != null) {
                hologram.renderToPlayer(bukkitPlayer);
            }
        }

        super.onPlayerRejoin(args);
    }

    @Override
    public boolean interact(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (args.getEvent() instanceof PlayerInteractAtEntityEvent event) {
            return event.getRightClicked().equals(armorStand);
        }

        return false;
    }
}
