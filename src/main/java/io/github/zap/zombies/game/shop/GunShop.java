package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.map.shop.GunShopData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Shop used to purchase guns
 */
public class GunShop extends ArmorStandShop<GunShopData> {

    private Item item = null;

    public GunShop(ZombiesArena zombiesArena, GunShopData shopData) {
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
        getArena().getShopEvent(ShopType.LUCKY_CHEST.name()).registerHandler(args -> {
            Player player = args.getZombiesPlayer().getPlayer();
            if (player != null) {
                displayToPlayer(player);
            }
        });
        getArena().getShopEvent(ShopType.PIGLIN_SHOP.name()).registerHandler(args -> {
            Player player = args.getZombiesPlayer().getPlayer();
            if (player != null) {
                displayToPlayer(player);
            }
        });
    }

    @Override
    public void display() {
        if (item == null) {
            World world = getArena().getWorld();

            ZombiesArena zombiesArena = getArena();
            EquipmentData<?> equipmentData = zombiesArena.getEquipmentManager()
                    .getEquipmentData(zombiesArena.getMap().getName(), getShopData().getGunName());

            if (equipmentData == null) {
                Zombies.warning("Unable to find equipment data for weapon " + getShopData().getGunName() + "!");
                return;
            }

            ItemStack itemStack = new ItemStack(equipmentData.getMaterial());
            item = world.dropItem(getShopData().getRootLocation().toLocation(world)
                            .add(new Vector(0.5D, 0, 0.5D)), itemStack);
            item.setGravity(false);
            item.setVelocity(new Vector(0, 0, 0));

            zombiesArena.getProtectedItems().add(item);
        }

        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine(Component.empty());
        }

        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        ZombiesPlayer zombiesPlayer =  getArena().getPlayerMap().get(player.getUniqueId());
        GunShopData gunShopData = getShopData();
        String gunName = gunShopData.getGunName();
        String gunDisplayName = gunShopData.getGunDisplayName();

        Component firstHologramComponent = null;
        Component secondHologramComponent = null;

        if (gunShopData.isRequiresPower() && !isPowered()) {
            secondHologramComponent = Component.text("Requires Power!", NamedTextColor.GRAY);
        } else {
            if (zombiesPlayer != null) {
                HotbarObjectGroup hotbarObjectGroup = zombiesPlayer.getHotbarManager()
                        .getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());
                if (hotbarObjectGroup != null) {
                    for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof Gun<?, ?> gun && gun.getEquipmentData().getName().equals(gunName)) {
                            firstHologramComponent = Component.text("Refill " + gunDisplayName,
                                    NamedTextColor.GREEN);
                            secondHologramComponent = Component.text(gunShopData.getRefillCost() + " Gold",
                                    NamedTextColor.GOLD);
                            break;
                        }
                    }
                }
            }
        }

        if (firstHologramComponent == null) {
            firstHologramComponent = Component.text("Buy " + gunDisplayName, NamedTextColor.GREEN);
            secondHologramComponent = Component.text(gunShopData.getCost() + " Gold", NamedTextColor.GOLD);
        }

        Hologram hologram = getHologram();

        hologram.updateLineForPlayer(player, 0, firstHologramComponent);
        hologram.updateLineForPlayer(player, 1, secondHologramComponent);
    }

    @Override
    public boolean interact(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.getManagedPlayer();
            if (player != null) {
                Player bukkitPlayer = player.getPlayer();

                if (bukkitPlayer != null) {
                    if (!getShopData().isRequiresPower() || isPowered()) {
                        HotbarManager hotbarManager = player.getHotbarManager();
                        GunObjectGroup gunObjectGroup = (GunObjectGroup)
                                hotbarManager.getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());

                        if (gunObjectGroup != null) {
                            Boolean refillAttempt = tryRefill(player, gunObjectGroup);
                            if (refillAttempt == null) {
                                if (tryBuy(player, gunObjectGroup)) {
                                    bukkitPlayer.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER,
                                            1.0F, 2.0F));
                                    onPurchaseSuccess(player);
                                    return true;
                                }
                            } else if (refillAttempt) {
                                bukkitPlayer.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER,
                                        1.0F, 2.0F));
                                onPurchaseSuccess(player);
                                return true;
                            }
                        } else {
                            bukkitPlayer.sendMessage(Component.text("You cannot purchase guns in this map",
                                    NamedTextColor.RED));
                        }
                    } else {
                        bukkitPlayer.sendMessage(Component.text("The power is not active yet!", NamedTextColor.RED));
                    }

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER,
                            1.0F, 0.5F));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.GUN_SHOP.name();
    }

    /**
     * Attempts to refill a gun
     * @param player The player attempting to refill the gun
     * @param gunObjectGroup The gun object group in which the gun may reside
     * @return Whether purchase was successful, or null if no interaction occurred
     */
    private Boolean tryRefill(ZombiesPlayer player, GunObjectGroup gunObjectGroup) {
        GunShopData gunShopData = getShopData();
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
                if (hotbarObject instanceof Gun<?, ?> gun
                        && gun.getEquipmentData().getName().equals(gunShopData.getGunName())) {
                    if (gun.getCurrentAmmo() == gun.getCurrentLevel().getAmmo()) {
                        bukkitPlayer.sendMessage(Component.text("Your gun is already filled!",
                                NamedTextColor.RED));
                        return false;
                    } else {
                        int refillCost = gunShopData.getRefillCost();
                        if (player.getCoins() < refillCost) {
                            bukkitPlayer.sendMessage(Component.text("You cannot afford this item!",
                                    NamedTextColor.RED));

                            return false;
                        } else {
                            player.subtractCoins(refillCost);
                            gun.refill();

                            return true;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Attempts to purchase a gun
     * @param player The player receiving the gun
     * @param gunObjectGroup The gun object group that receives the gun
     * @return Whether purchase was successful
     */
    private boolean tryBuy(@NotNull ZombiesPlayer player, @NotNull GunObjectGroup gunObjectGroup) {
        Player bukkitPlayer = player.getPlayer();
        GunShopData gunShopData = getShopData();

        if (bukkitPlayer != null) {
            Integer slot = gunObjectGroup.getNextEmptySlot();
            if (slot == null) {
                int selectedSlot = bukkitPlayer.getInventory().getHeldItemSlot();
                if (gunObjectGroup.getHotbarObjectMap().containsKey(selectedSlot)) {
                    slot = selectedSlot;
                } else {
                    bukkitPlayer.sendMessage(Component.text("Choose the slot you want to buy the gun in!",
                            NamedTextColor.RED));
                    return false;
                }
            }

            int cost = gunShopData.getCost();
            if (player.getCoins() < cost) {
                bukkitPlayer.sendMessage(Component.text("You cannot afford this item!", NamedTextColor.RED));
                return false;
            } else {
                player.subtractCoins(getShopData().getCost());
                gunObjectGroup.setHotbarObject(slot, getArena().getEquipmentManager().createEquipment(getArena(),
                        player, slot, getArena().getMap().getName(), gunShopData.getGunName()));
                return true;
            }
        }

        return false;
    }

}
