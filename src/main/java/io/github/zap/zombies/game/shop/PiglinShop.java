package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.DisposableBukkitRunnable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.map.shop.PiglinShopData;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PiglinShop extends Shop<PiglinShopData> {

    private final static Random RANDOM = new Random();

    private final Piglin dream;

    private final Hologram hologram;

    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    private EquipmentData<?> equipmentData;

    private Sitter sitter;

    private boolean init = false;

    private boolean active = false;

    private boolean doneThinking = false;

    private Player roller;

    public PiglinShop(ZombiesArena arena, PiglinShopData shopData) {
        super(arena, shopData);

        this.dream = Zombies.getInstance().getNmsBridge().entityBridge().makeDream(arena.getWorld());
        this.hologram = new Hologram(shopData.getPiglinLocation().add(new Vector(0, 1, 0)).toLocation(arena.getWorld()));
        for (String equipmentName : shopData.getEquipments()) {
            this.equipments.add(arena.getEquipmentManager().getEquipmentData(arena.getMap().getName(), equipmentName));
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

            hologram.updateLineForEveryone(0, MiniMessage.get().parse("<gold><bold>Lucky Swine"));
            hologram.updateLineForEveryone(1,
                    getShopData().isRequiresPower() && !isPowered()
                            ? MiniMessage.get().parse("<gray>Requires Power!")
                            : MiniMessage.get().parse("<yellow><bold>" + getShopData().getCost() + " Gold"));

        }
        if (!init) {
            Zombies.getInstance().getNmsBridge().entityBridge().spawnDream(dream, getArena().getWorld());
            dream.teleportAsync(new Location(getArena().getWorld(), getShopData().getPiglinLocation().getX(),
                    getShopData().getPiglinLocation().getY(), getShopData().getPiglinLocation().getZ(),
                    getShopData().getDirection(), 0.0F));
            init = true;
        }

        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        super.displayToPlayer(player);
        hologram.renderToPlayer(player);
    }

    @Override
    public boolean interact(ManagingArena<ZombiesArena, ZombiesPlayer>.ProxyArgs<? extends Event> args) {
        if (args.getEvent() instanceof PlayerInteractEntityEvent event && event.getRightClicked().getUniqueId().equals(dream.getUniqueId())) {
            ZombiesPlayer player = args.getManagedPlayer();

            if (player != null) {
                Player bukkitPlayer = player.getPlayer();

                if (bukkitPlayer != null) {
                    if (getShopData().isRequiresPower() && !isPowered()) {
                        bukkitPlayer.sendMessage(MiniMessage.get().parse("<red>I need some power to trade!"));
                    } else if (!active) {
                        String notActive = "Shop's not open right now.";
                        String piglinRoom = getArena().getPiglinRoom();
                        if (piglinRoom != null) {
                            notActive += " Go to " + piglinRoom + ", will ya?";
                        }

                        bukkitPlayer.sendMessage(MiniMessage.get().parse("<red>" + notActive));
                    } else if (roller != null) {
                        if (bukkitPlayer.equals(roller)) {
                            if (doneThinking) {
                                if (attemptToClaim(player)) {
                                    return true;
                                }
                            } else {
                                bukkitPlayer.sendMessage(MiniMessage.get()
                                        .parse("<red>Hey, let me think what to give you!"));
                            }
                        } else {
                            bukkitPlayer.sendMessage(MiniMessage.get()
                                    .parse("<red>I'm trading with someone else!"));
                        }
                    } else {
                        int cost = getShopData().getCost();
                        if (args.getManagedPlayer().getCoins() < cost) {
                            bukkitPlayer.sendMessage(MiniMessage.get()
                                    .parse("<red>You don't have enough coins to trade with me!"));
                        } else {
                            player.subtractCoins(cost);

                            hologram.destroy();
                            roller = bukkitPlayer;
                            doneThinking = false;

                            EntityEquipment initialEquipment = dream.getEquipment();
                            if (initialEquipment != null) {
                                initialEquipment.setItemInOffHand(new ItemStack(Material.GOLD_INGOT));
                            }
                            getArena().runTaskLater(120L, () -> {
                                equipmentData = equipments.get(RANDOM.nextInt(equipments.size()));
                                EntityEquipment equipment = dream.getEquipment();
                                if (equipment != null) {
                                    equipment.setItemInOffHand(null);
                                    equipment.setItemInMainHand(new ItemStack(equipmentData.getMaterial()));
                                }

                                getArena().runTaskTimer(0L, 2L, sitter = new Sitter(player));
                            });

                            return true;
                        }
                    }

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.piglin.angry"),
                            Sound.Source.MASTER, 1.0F, 1.0F));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.PIGLIN_SHOP.name();
    }

    /**
     * Attempts to claim the piglin weapon
     *
     * @param player The claimant player
     * @return Whether claim was successful
     */
    private boolean attemptToClaim(@NotNull ZombiesPlayer player) {
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
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

                    sitter.destroy();

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"), Sound.Source.MASTER,
                            1.0F, 2.0F));

                    return true;
                } else {
                    bukkitPlayer.sendMessage(MiniMessage.get()
                            .parse("<red>Choose a slot to receive the item in!"));
                }
            } else {
                bukkitPlayer.sendMessage(MiniMessage.get().parse("<red>You can't claim this weapon!"));
                sitter.destroy();
            }
        }

        return false;
    }

    /**
     * Attempts to refill a gun upon claim
     *
     * @param hotbarObjectGroup The object group in which the gun may reside
     * @param equipmentData     The equipment data of the gun
     * @return Whether refill was successful
     */
    private boolean attemptToRefill(@NotNull HotbarObjectGroup hotbarObjectGroup,
                                    @NotNull EquipmentData<?> equipmentData) {
        for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
            if (hotbarObject instanceof Gun<?, ?> gun &&
                    gun.getEquipmentData().getName().equals(equipmentData.getName())) {
                gun.refill();

                sitter.destroy();

                hotbarObjectGroup.getPlayer().playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"),
                        Sound.Source.MASTER, 1.0F, 2.0F));

                return true;
            }
        }

        return false;
    }

    private class Sitter extends DisposableBukkitRunnable {

        private final ZombiesPlayer player;

        private final Hologram endHologram = new Hologram(getShopData().getPiglinLocation().add(new Vector(0, 1, 0)).toLocation(getArena().getWorld()));

        private long sittingTime = getShopData().getSittingTime();

        public Sitter(ZombiesPlayer player) {
            this.player = player;

            doneThinking = true;
            endHologram.addLine(MiniMessage.get().parse("<red>Right Click to Claim!"));
            endHologram.addLine(Component.empty());
            endHologram.addLine(MiniMessage.get().parse("<yellow>" + equipmentData.getDisplayName()));

            Component equipmentName = Component.text(equipmentData.getDisplayName(), NamedTextColor.YELLOW);
            Component exclamationPoint = Component.text("!", NamedTextColor.RED);
            if (roller.isOnline()) {
                roller.sendMessage(MiniMessage.get().parse("<red>You got a <reset><equipment><reset><red>!",
                        Template.of("equipment", equipmentName)));
            }
            Component message = MiniMessage.get().parse("<red><roller> <reset><red>got a " +
                            "<reset><equipment><reset><red>!",
                    Template.of("roller", equipmentName), Template.of("equipment", equipmentName));
            for (Player otherPlayer : roller.getWorld().getPlayers()) {
                if (!otherPlayer.equals(roller)) {
                    otherPlayer.sendMessage(message);
                }
            }
        }

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

        public void destroy() {
            endHologram.destroy();
            equipmentData = null;
            roller = null;
            EntityEquipment equipment = dream.getEquipment();
            if (equipment != null) {
                equipment.setItemInMainHand(null);
            }

            display();
            onPurchaseSuccess(player);

            cancel();
        }
    }

}
