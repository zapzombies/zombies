package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.shop.LuckyChestData;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.util.Jingle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Chest used to randomly generate a weapon from a predefined set of weapons to present to the player
 */
public class LuckyChest extends Shop<LuckyChestData> {

    private final Location chestLocation;

    private final Block left, right;

    private final Hologram hologram;

    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    private boolean active = false;

    private GunSwapper gunSwapper;

    private boolean doneRolling = false;

    private Player roller;

    public LuckyChest(ZombiesArena zombiesArena, LuckyChestData shopData) {
        super(zombiesArena, shopData);

        Vector chestLocation = shopData.getChestLocation();
        World world = zombiesArena.getWorld();
        Block block = world.getBlockAt(chestLocation.getBlockX(), chestLocation.getBlockY(), chestLocation.getBlockZ());

        Chest chest = (Chest) block.getState();
        DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();

        // TODO: validate data
        DoubleChestInventory doubleChestInventory = (DoubleChestInventory) doubleChest.getInventory();
        Location leftLocation = doubleChestInventory.getLeftSide().getLocation();
        Location rightLocation = doubleChestInventory.getRightSide().getLocation();

        left = world.getBlockAt(leftLocation);
        right = world.getBlockAt(rightLocation);

        this.chestLocation = leftLocation
                .add(0.5, 0, 0.5)
                .add(rightLocation.add(0.5, 0, 0.5))
                .multiply(0.5);

        hologram = new Hologram(this.chestLocation.clone());

        EquipmentManager equipmentManager = zombiesArena.getEquipmentManager();
        String mapName = zombiesArena.getMap().getName();
        for (String equipmentName : shopData.getEquipments()) {
            equipments.add(equipmentManager.getEquipmentData(mapName, equipmentName));
        }
    }

    public void setActive(boolean active) {
        if (active) {
            if (!this.active) {
                this.active = true;
                display();
            }
        } else if (this.active) {
            hologram.destroy();
            this.active = false;
            display();
        }
    }

    @Override
    public void display() {
        if (active && roller == null) {
            while (hologram.getHologramLines().size() < 2) {
                hologram.addLine(Component.empty());
            }

            hologram.updateLineForEveryone(0, Component.text("Lucky Chest", NamedTextColor.GOLD,
                    TextDecoration.BOLD));
            hologram.updateLineForEveryone(1,
                    getShopData().isRequiresPower() && !isPowered()
                            ? Component.text("Requires Power!", NamedTextColor.GRAY)
                            : Component.text(getShopData().getCost() + " Gold", NamedTextColor.YELLOW,
                            TextDecoration.BOLD));

        }

        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        super.displayToPlayer(player);
        hologram.renderToPlayer(player);
        if (gunSwapper != null) {
            gunSwapper.renderToPlayer(player);
        }
    }

    @Override
    public boolean interact(ZombiesArena.ProxyArgs<? extends Event> args) {
        Event event = args.getEvent();
        if (event instanceof PlayerInteractEvent playerInteractEvent) {
            Block clickedBlock = playerInteractEvent.getClickedBlock();

            if (left.equals(clickedBlock) || right.equals(clickedBlock)) {
                LuckyChestData luckyChestData = getShopData();
                ZombiesPlayer player = args.getManagedPlayer();

                if (player != null) {
                    Player bukkitPlayer = player.getPlayer();

                    if (bukkitPlayer != null) {
                        if (luckyChestData.isRequiresPower() && !isPowered()) {
                            bukkitPlayer.sendMessage(Component.text("The power is not turned on!",
                                    NamedTextColor.RED));
                        } else if (!active) {
                            String notActive = "This Lucky Chest is not active right now!";
                            String luckyChestRoom = getArena().getLuckyChestRoom();
                            if (luckyChestRoom != null) {
                                notActive += " Find the Lucky Chest in " + luckyChestRoom + "!";
                            }

                            bukkitPlayer.sendMessage(Component.text(notActive, NamedTextColor.RED));
                        } else if (roller != null) {
                            if (bukkitPlayer.equals(roller)) {
                                if (doneRolling) {
                                    if (attemptToClaim(player)) {
                                        return true;
                                    }
                                } else {
                                    bukkitPlayer.sendMessage(Component.text("The chest is not done rolling!")
                                            .color(NamedTextColor.RED));
                                }
                            } else {
                                bukkitPlayer.sendMessage(Component.text("Someone else is rolling!")
                                        .color(NamedTextColor.RED));
                            }
                        } else {
                            int cost = getShopData().getCost();
                            if (args.getManagedPlayer().getCoins() < cost) {
                                bukkitPlayer.sendMessage(Component
                                        .text("You don't have enough coins to do that!", NamedTextColor.RED));
                            } else {
                                player.subtractCoins(cost);

                                hologram.destroy();
                                roller = bukkitPlayer;
                                doneRolling = false;

                                Jingle.play(getArena(), getShopData().getJingle(),
                                        gunSwapper = new GunSwapper(player),
                                        chestLocation.clone().add(0, 1, 0));

                                return true;
                            }
                        }

                        bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                                Sound.Source.MASTER, 1.0F, 0.5F));
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.LUCKY_CHEST.name();
    }

    /**
     * Attempts to claim the lucky chest weapon
     * @param player The claimant player
     * @return Whether claim was successful
     */
    private boolean attemptToClaim(@NotNull ZombiesPlayer player) {
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            EquipmentData<?> equipmentData = gunSwapper.currentEquipment;
            HotbarObjectGroup equipmentObjectGroup = player.getHotbarManager()
                    .getHotbarObjectGroup(equipmentData.getEquipmentObjectGroupType());

            if (equipmentObjectGroup != null) {
                if (attemptToRefill(equipmentObjectGroup, equipmentData)) {
                    return true;
                }

                Integer nextSlot = equipmentObjectGroup.getNextEmptySlot();
                if (nextSlot == null) {
                    int heldSlot = bukkitPlayer.getInventory().getHeldItemSlot();
                    if (equipmentObjectGroup.getHotbarObjectMap().containsKey(heldSlot)) {
                        nextSlot = heldSlot;
                    }
                }
                if (nextSlot != null) {
                    ZombiesArena zombiesArena = getArena();
                    equipmentObjectGroup.setHotbarObject(nextSlot,
                            zombiesArena.getEquipmentManager().createEquipment(zombiesArena,
                                    player, nextSlot, equipmentData));

                    gunSwapper.destroy();

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"), Sound.Source.MASTER,
                            1.0F, 2.0F));

                    return true;
                } else {
                    bukkitPlayer.sendMessage(Component.text("Choose a slot to receive the item in!",
                                    NamedTextColor.RED));
                }
            } else {
                bukkitPlayer.sendMessage(Component.text("You can't claim this weapon!")
                        .color(NamedTextColor.RED));
                gunSwapper.destroy();
            }
        }

        return false;
    }

    /**
     * Attempts to refill a gun upon claim
     * @param hotbarObjectGroup The object group in which the gun may reside
     * @param equipmentData The equipment data of the gun
     * @return Whether refill was successful
     */
    private boolean attemptToRefill(@NotNull HotbarObjectGroup hotbarObjectGroup,
                                    @NotNull EquipmentData<?> equipmentData) {
        for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
            if (hotbarObject instanceof Gun<?, ?> gun &&
                    gun.getEquipmentData().getName().equals(equipmentData.getName())) {
                gun.refill();

                gunSwapper.destroy();

                hotbarObjectGroup.getPlayer().playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"),
                        Sound.Source.MASTER, 1.0F, 2.0F));

                return true;
            }
        }

        return false;
    }

    private class GunSwapper implements Jingle.JingleListener {

        private final Random random = new Random();

        private final PacketContainer openChestContainer = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);

        private final ZombiesPlayer zombiesPlayer;

        private final Item item;

        private EquipmentData<?> currentEquipment;

        private int sittingTaskId;

        private final Hologram equipmentName;

        private final Hologram endHologram;

        public GunSwapper(ZombiesPlayer zombiesPlayer) {
            this.zombiesPlayer = zombiesPlayer;

            World world = getArena().getWorld();
            openChestContainer.getBlockPositionModifier().write(0,
                    new BlockPosition(chestLocation.toVector()));
            openChestContainer.getIntegers()
                    .write(0, 1)
                    .write(1, 1);
            for (Player player : world.getPlayers()) {
                showChestToPlayer(player);
            }

            item = world.dropItem(chestLocation.clone().add(0, 1, 0),
                    new ItemStack((currentEquipment = equipments.get(random.nextInt(equipments.size())))
                            .getMaterial()));
            item.setGravity(false);
            item.setVelocity(new Vector(0, 0, 0));

            equipmentName = new Hologram(chestLocation.clone().subtract(0, 0.75, 0));
            equipmentName.addLine(Component.text(currentEquipment.getDisplayName(),
                    NamedTextColor.YELLOW));

            endHologram = new Hologram(chestLocation);
        }

        private void renderToPlayer(Player player) {
            equipmentName.renderToPlayer(player);
            showChestToPlayer(player);
        }

        private void showChestToPlayer(Player player) {
            ArenaApi.getInstance().sendPacketToPlayer(Zombies.getInstance(), player, openChestContainer);
        }

        @Override
        public void onNotePlayed(List<Jingle.Note> jingle) {
            currentEquipment = equipments.get(random.nextInt(equipments.size()));
            equipmentName.updateLineForEveryone(0, Component.text(currentEquipment.getDisplayName(),
                    NamedTextColor.RED));

            item.setItemStack(new ItemStack(currentEquipment.getMaterial()));
        }

        @Override
        public void onEnd(List<Jingle.Note> jingle) {
            doneRolling = true;

            endHologram.addLine(Component.text("Right Click to Claim!", NamedTextColor.RED));
            endHologram.addLine(Component.empty());

            Component equipmentName = Component.text(currentEquipment.getDisplayName(), NamedTextColor.YELLOW);
            Component inTheLuckyChest = Component.text(" in the Lucky Chest!", NamedTextColor.RED);
            if (roller.isOnline()) {
                roller.sendMessage(TextComponent.ofChildren(
                        Component.text("You found ", NamedTextColor.RED),
                        equipmentName,
                        inTheLuckyChest
                ));
            }
            Component message = TextComponent.ofChildren(
                    roller.displayName(),
                    Component.text(" found ", NamedTextColor.RED),
                    equipmentName,
                    inTheLuckyChest
            );
            for (Player player : roller.getWorld().getPlayers()) {
                if (!player.equals(roller)) {
                    player.sendMessage(message);
                }
            }

            sittingTaskId = getArena().runTaskTimer(0L, 2L, new Runnable() {

                private long sittingTime = getShopData().getSittingTime();

                @Override
                public void run() {
                    if (sittingTime > 0) {
                        endHologram.updateLineForEveryone(1,
                                Component.text(TimeUtil.convertTicksToSecondsString(sittingTime),
                                        NamedTextColor.RED));

                        sittingTime -= 2;
                    } else {
                        destroy();
                    }
                }

            }).getTaskId();
        }


        public void destroy() {
            openChestContainer.getIntegers().write(1, 0);
            for (Player player : chestLocation.getWorld().getPlayers()) {
                showChestToPlayer(player);
            }

            equipmentName.destroy();
            endHologram.destroy();

            item.remove();
            Bukkit.getScheduler().cancelTask(sittingTaskId);

            roller = null;
            gunSwapper = null;
            display();

            onPurchaseSuccess(zombiesPlayer);
        }

    }

}
