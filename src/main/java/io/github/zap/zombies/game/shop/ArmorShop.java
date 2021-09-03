package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hologram.HologramLine;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.ArmorShopData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shop for purchasing pieces of armor at a time
 */
public class ArmorShop extends ArmorStandShop<ArmorShopData> {

    private final static Map<Integer, ArmorShop> ARMOR_STAND_ID_ARMOR_SHOP_MAP = new HashMap<>();

    private static final Map<Integer, EnumWrappers.ItemSlot> ITEM_SLOT_MAP = new HashMap<>() {
        {
            put(2, EnumWrappers.ItemSlot.FEET);
            put(3, EnumWrappers.ItemSlot.LEGS);
            put(4, EnumWrappers.ItemSlot.CHEST);
            put(5, EnumWrappers.ItemSlot.HEAD);
        }
    };

    private final ProtocolManager protocolManager;

    static {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Zombies.getInstance(),
                PacketType.Play.Server.SPAWN_ENTITY_LIVING, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packetContainer = event.getPacket();
                if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
                    ArmorShop armorShop = ARMOR_STAND_ID_ARMOR_SHOP_MAP
                            .get(packetContainer.getIntegers().read(0));

                    if (armorShop != null) {
                        NetworkMarker networkMarker = event.getNetworkMarker();
                        networkMarker.addPostListener(new PacketPostAdapter(Zombies.getInstance()) {
                            @Override
                            public void onPostEvent(PacketEvent packetEvent) {
                                armorShop.displayToPlayer(event.getPlayer());
                            }
                        });
                    }
                }
            }
        });
    }

    public ArmorShop(ZombiesArena zombiesArena, ArmorShopData shopData) {
        super(zombiesArena, shopData);
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        ArmorStand armorStand = getArmorStand();
        Location armorStandLocation = getArmorStand().getLocation().clone();
        armorStandLocation.add(0, 1.5, 0);
        armorStandLocation.setYaw(getShopData().getArmorStandDirection());
        armorStand.teleport(armorStandLocation);
        armorStand.setSmall(true);

        ARMOR_STAND_ID_ARMOR_SHOP_MAP.put(armorStand.getEntityId(), this);
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();
        getArena().getShopEvent(getShopType()).registerHandler(args -> display());
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();

        List<HologramLine<?>> lines = hologram.getHologramLines();
        while (lines.size() < 2) {
            hologram.addLine(Component.empty());
        }

        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        Hologram hologram = getHologram();
        ArmorShopData armorShopData = getShopData();

        List<ArmorShopData.ArmorLevel> armorLevels = armorShopData.getArmorLevels();
        ArmorShopData.ArmorLevel armorLevel = determineArmorLevel(player);

        // Display the hologram
        Component secondHologramComponent;
        if (armorLevel == null) {
            armorLevel = armorLevels.get(armorLevels.size() - 1);
            secondHologramComponent = Component.text("You have already unlocked this item!",
                    NamedTextColor.RED);
        } else {
            secondHologramComponent = (armorShopData.isRequiresPower() && !isPowered())
                    ? Component.text("Requires Power!", NamedTextColor.GRAY)
                    : Component.text(armorLevel.getCost() + " Gold", NamedTextColor.GOLD);
        }

        sendArmorStandUpdatePackets(player, armorLevel);

        hologram.updateLineForPlayer(player, 0, Component.text(armorLevel.getName(), NamedTextColor.GREEN));
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
                        ArmorShopData.ArmorLevel armorLevel = determineArmorLevel(bukkitPlayer);

                        if (armorLevel == null) {
                            bukkitPlayer.sendMessage(Component
                                    .text("You already have the max level of this armor!", NamedTextColor.RED));
                        } else {
                            int cost = armorLevel.getCost();

                            if (player.getCoins() < cost) {
                                bukkitPlayer.sendMessage(Component.text("You cannot afford this item!",
                                        NamedTextColor.RED));
                            } else {
                                applyArmor(player, armorLevel);

                                bukkitPlayer.playSound(Sound.sound(Key.key("block.note_block.pling"),
                                        Sound.Source.MASTER, 1.0F, 2.0F));

                                player.subtractCoins(cost);
                                onPurchaseSuccess(player);

                                return true;
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
        return ShopType.ARMOR_SHOP.name();
    }

    /**
     * Applies an armor level to the zombies player (does not update other armor shops)
     * @param player The player to apply the armor to
     * @param armorLevel The armor level to apply
     */
    private void applyArmor(@NotNull ZombiesPlayer player, @NotNull ArmorShopData.ArmorLevel armorLevel) {
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            // Choose the best equipments
            Material[] materials = armorLevel.getMaterials();
            //noinspection ConstantConditions
            ItemStack[] current = bukkitPlayer.getEquipment().getArmorContents();
            for (int i = 0; i < 4; i++) {
                Material material = materials[i];
                ItemStack itemStack = current[i];

                if (material != null) {
                    if (itemStack != null
                            && itemStack.getType().getMaxDurability() < material.getMaxDurability()) {
                        itemStack.setType(material);
                    } else {
                        current[i] = new ItemStack(material);
                    }
                }

            }

            player.updateEquipment(current);
        }
    }

    /**
     * Determines the relevant armor level of the player
     * @param player The player to determine the armor level for
     * @return The armor level of the player, or null if the player's armor is better than the shop can provide
     */
    private ArmorShopData.ArmorLevel determineArmorLevel(Player player) {
        //noinspection ConstantConditions
        ItemStack[] equipment = player.getEquipment().getArmorContents();
        for (ArmorShopData.ArmorLevel armorLevel : getShopData().getArmorLevels()) {
            Material[] materials = armorLevel.getMaterials();
            for (int i = 0; i < 4; i++) {
                Material material = materials[i];
                ItemStack itemStack = equipment[i];

                // Accept any material that overrides a current item stack's durability or lack thereof
                if (material != null
                        &&
                        (itemStack == null || material.getMaxDurability() > itemStack.getType().getMaxDurability())) {
                    return armorLevel;
                }
            }
        }

        return null;
    }

    /**
     * Sends packets relating to a player's current armor and the armor shop itself
     * @param player The player to send the packets to
     * @param armorLevel The armor level to compare the player's armor against
     */
    private void sendArmorStandUpdatePackets(Player player, ArmorShopData.ArmorLevel armorLevel) {
        //noinspection ConstantConditions
        ItemStack[] equipment = player.getEquipment().getArmorContents();
        Material[] materials = armorLevel.getMaterials();

        int armorStandId = getArmorStand().getEntityId();

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentSlotStackPairList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Material material = materials[i];
            ItemStack playerItemStack = equipment[i];
            ItemStack itemStack = playerItemStack == null ? null : playerItemStack.clone();

            if (material != null) {
                if (itemStack != null) {
                    itemStack.setType(material);
                } else {
                    itemStack = new ItemStack(material);
                }
            }

            equipmentSlotStackPairList.add(new Pair<>(ITEM_SLOT_MAP.get(i + 2), itemStack));
        }

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, armorStandId);
        packetContainer.getSlotStackPairLists().write(0, equipmentSlotStackPairList);

        try {
            protocolManager.sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException exception) {
            Zombies.warning(
                    String.format("Error creating armor shop equipment packets for entity id %d", armorStandId)
            );
        }
    }

}
