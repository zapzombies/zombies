package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.PerkMachineData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.perk.Perk;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Machine used to purchase or upgrade perks
 */
public class PerkMachine extends BlockShop<PerkMachineData>  {

    public PerkMachine(ZombiesArena zombiesArena, PerkMachineData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();

        getArena().getShopEvent(getShopType()).registerHandler(args -> {
            Player player = args.getZombiesPlayer().getPlayer();
            if (player != null) {
                displayToPlayer(player);
            }
        });
        for (ZombiesPlayer player : getArena().getPlayerMap().values()) {
            player.getStateChangedEvent().registerHandler(state -> {
                Player bukkitPlayer = player.getPlayer();
                if (bukkitPlayer != null) {
                    displayToPlayer(bukkitPlayer);
                }
            });
        }
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine(Component.empty());
        }
        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        ZombiesPlayer zombiesPlayer = getArena().getPlayerMap().get(player.getUniqueId());
        PerkMachineData perkMachineData = getShopData();
        Perk<?, ?, ?, ?> perk = determinePerk(zombiesPlayer);

        int level = (perk == null) ? 0 : perk.getLevel() + 1;

        Component secondHologramComponent;
        if (perk == null || level < perk.getEquipmentData().getLevels().size()) {
            secondHologramComponent = perkMachineData.isRequiresPower() && !isPowered()
                    ? Component.text("Requires Power!", NamedTextColor.GRAY)
                    : Component.text(perkMachineData.getCosts().get(level) + " Gold", NamedTextColor.GOLD);
        } else {
            secondHologramComponent = Component.text("Active", NamedTextColor.GREEN);
        }


        Hologram hologram = getHologram();
        hologram.updateLineForPlayer(player, 0, Component.text("Buy " + perkMachineData.getPerkName(),
                NamedTextColor.BLUE));
        hologram.updateLineForPlayer(player, 1, secondHologramComponent);
    }

    @Override
    public boolean interact(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.getManagedPlayer();

            if (player != null) {
                Player bukkitPlayer = player.getPlayer();

                if (bukkitPlayer != null) {
                    PerkMachineData perkMachineData = getShopData();
                    Perk<?, ?, ?, ?> perk = determinePerk(player);

                    if (!perkMachineData.isRequiresPower() || isPowered()) {
                        int level;
                        List<Integer> costs = perkMachineData.getCosts();

                        if (perk == null) {
                            if (!costs.isEmpty()) {
                                int cost = costs.get(0);

                                if (player.getCoins() < cost) {
                                    bukkitPlayer.sendMessage(Component.text("You cannot afford this item!",
                                            NamedTextColor.RED));
                                } else if (attemptToBuyPerk(player)) {
                                    return true;
                                }
                            }
                        } else {
                            level = perk.getLevel() + 1;

                            if (level < costs.size()) {
                                int cost = costs.get(level);

                                if (player.getCoins() < cost) {
                                    bukkitPlayer.sendMessage(Component.text("You cannot afford this item!",
                                            NamedTextColor.RED));
                                } else {
                                    player.subtractCoins(cost);
                                    perk.upgrade();

                                    onPurchaseSuccess(player);
                                    return true;
                                }
                            } else {
                                bukkitPlayer.sendMessage(Component.text("You have already maxed out this item!",
                                        NamedTextColor.RED));
                            }
                        }
                    } else {
                        bukkitPlayer.sendMessage(Component.text("The power is not active yet!",
                                NamedTextColor.RED));
                    }

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER, 1.0F, 0.5F));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.PERK_MACHINE.name();
    }

    /**
     * Finds the corresponding perk equipment within a player's hotbar
     * @param zombiesPlayer The player to search for the equipment in
     * @return The perk equipment, or null if it doesn't exist
     */
    private Perk<?, ?, ?, ?> determinePerk(ZombiesPlayer zombiesPlayer) {
        if (zombiesPlayer != null) {
            EquipmentObjectGroup equipmentObjectGroup = (EquipmentObjectGroup)
                    zombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());

            if (equipmentObjectGroup != null) {
                for (HotbarObject hotbarObject : equipmentObjectGroup.getHotbarObjectMap().values()) {
                    if (hotbarObject instanceof Perk<?, ?, ?, ?> perk &&
                            perk.getEquipmentData().getName().equals(getShopData().getPerkName())) {
                        return perk;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Attempts to purchase the perk for the first time for a player
     * @param player The purchasing player
     * @return Whether purchase was successful
     */
    private boolean attemptToBuyPerk(@NotNull ZombiesPlayer player) {
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            HotbarManager hotbarManager = player.getHotbarManager();
            HotbarObjectGroup hotbarObjectGroup = hotbarManager
                    .getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());
            if (hotbarObjectGroup != null) {
                Integer slot = hotbarObjectGroup.getNextEmptySlot();
                if (slot == null) {
                    int heldSlot = bukkitPlayer.getInventory().getHeldItemSlot();
                    if (hotbarObjectGroup.getHotbarObjectMap().containsKey(heldSlot)) {
                        slot = heldSlot;
                    }
                }

                if (slot != null) {
                    ZombiesArena arena = getArena();
                    hotbarManager.setHotbarObject(slot, arena.getEquipmentManager()
                            .createEquipment(arena, player, slot, arena.getMap().getName(),
                                    getShopData().getPerkName()));

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.firework_rocket.twinkle"),
                            Sound.Source.MASTER, 1.0F, 1.0F));

                    player.subtractCoins(getShopData().getCosts().get(0));
                    onPurchaseSuccess(player);

                    return true;
                } else {
                    bukkitPlayer.sendMessage(Component.text("Choose a slot to receive the perk in!",
                            NamedTextColor.RED));
                }
            } else {
                bukkitPlayer.sendMessage(Component.text("You cannot receive this item!", NamedTextColor.RED));
            }
        }

        return false;
    }

}
