package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.PowerSwitchData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Switch used to turn on the power in the arena permanently
 */
public class PowerSwitch extends BlockShop<PowerSwitchData> {

    public PowerSwitch(ZombiesArena zombiesArena, PowerSwitchData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine(Component.empty());
        }

        hologram.updateLineForEveryone(0, Component.text("Power Switch", NamedTextColor.GOLD));
        hologram.updateLineForEveryone(1,
                isPowered()
                        ? Component.text("Active", NamedTextColor.GREEN)
                        : Component.text(getShopData().getCost() + " Gold", NamedTextColor.GOLD)
                );
    }

    @Override
    public boolean interact(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.getManagedPlayer();

            if (player != null) {
                Player bukkitPlayer = player.getPlayer();

                if (bukkitPlayer != null) {
                    if (isPowered()) {
                        bukkitPlayer.sendMessage(Component.text("You have already turned on the power!",
                                NamedTextColor.RED));
                    } else {
                        int cost = getShopData().getCost();

                        if (player.getCoins() < cost) {
                            bukkitPlayer.sendMessage(Component.text("You cannot afford this item!",
                                    NamedTextColor.RED));
                        } else {
                            notifyPowerTurnedOn(bukkitPlayer);

                            player.subtractCoins(cost);
                            onPurchaseSuccess(player);
                            return true;
                        }
                    }

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER, 1.0F, 0.5F));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.POWER_SWITCH.name();
    }

    /**
     * Notifies all players in the world that the power is turned on
     * @param activator The player that turned on the power
     */
    private void notifyPowerTurnedOn(@NotNull Player activator) {
        for (Player playerInWorld : getArena().getWorld().getPlayers()) {
            playerInWorld.showTitle(Title.title(activator.displayName(),
                    Component.text("turned on the power!", NamedTextColor.GOLD),
                    Title.Times.of(Ticks.duration(20L), Ticks.duration(60L), Ticks.duration(20L))));
            playerInWorld.playSound(Sound.sound(Key.key("minecraft:entity.blaze.death"),
                    Sound.Source.MASTER, 1.0F, 2.0F));
        }
    }
}
