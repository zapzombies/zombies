package io.github.zap.zombies.game.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.SimpleJoinable;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.party.Party;
import io.github.zap.party.plugin.PartyPlugin;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * NPC that allows you to join a zombies game
 */
public class ZombiesNPC implements Listener {

    private final static String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private final static String PLAY_ZOMBIES = "Play Zombies";

    private final static String REJOIN_ZOMBIES = "Rejoin Zombies";

    private final static Map<Integer, ZombiesNPC> NPC_MAP = new HashMap<>();

    private final static PacketAdapter packetAdapter
            = new PacketAdapter(Zombies.getInstance(), PacketType.Play.Client.USE_ENTITY) {

        @Override
        public void onPacketReceiving(PacketEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                int id = event.getPacket().getIntegers().read(0);

                ZombiesNPC zombiesNPC = NPC_MAP.get(id);
                if (zombiesNPC != null) {
                    Player player = event.getPlayer();

                    Bukkit.getScheduler().runTask(Zombies.getInstance(),
                            () -> player.openInventory(zombiesNPC.mapInventory));
                }
            }
        }

    };

    private final Location location;

    private final int id;

    private final Inventory mapInventory;

    private final ItemStack rejoinButton, returnButton;

    private final Map<Integer, String> mapNameMap = new HashMap<>();

    private final PacketContainer playerAddPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

    private final PacketContainer playerRemovePacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

    private final PacketContainer spawnPacket;

    private final PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

    private final PacketContainer headRotationPacket
            = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

    private final PacketContainer lookPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);

    private final boolean isPlayer;

    public ZombiesNPC(World world, ZombiesNPCData data) {
        this.location = data.location.toLocation(world);

        EntityBridge entityBridge = ArenaApi.getInstance().getNmsBridge().entityBridge();
        this.id = entityBridge.nextEntityID();

        // potentially init packet listener
        NPC_MAP.put(id, this);
        if (NPC_MAP.size() == 1) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(packetAdapter);
        }

        UUID uniqueId = entityBridge.randomUUID();
        metadataPacket.getIntegers().write(0, id);
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        if (data.getEntityType() == EntityType.PLAYER) {
            isPlayer = true;

            // init player info data
            WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uniqueId, PLAY_ZOMBIES);
            WrappedSignedProperty texture = data.texture;
            if (texture != null) {
                wrappedGameProfile.getProperties().put("textures", texture);
            }

            PlayerInfoData playerInfoData = new PlayerInfoData(wrappedGameProfile, 0,
                    EnumWrappers.NativeGameMode.NOT_SET, WrappedChatComponent.fromText(PLAY_ZOMBIES));
            List<PlayerInfoData> playerInfoDataList = Collections.singletonList(playerInfoData);


            // init player add packet
            playerAddPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
            playerAddPacket.getPlayerInfoDataLists().write(0, playerInfoDataList);


            // init player remove packet
            playerRemovePacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
            playerRemovePacket.getPlayerInfoDataLists().write(0, playerInfoDataList);


            // set spawn packet
            spawnPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);


            // init metadata packet
            WrappedDataWatcher.Serializer overlaySerializer = WrappedDataWatcher.Registry.get(Byte.class);
            WrappedDataWatcher.WrappedDataWatcherObject overlay
                    = new WrappedDataWatcher.WrappedDataWatcherObject(16, overlaySerializer);

            wrappedDataWatcher.setObject(overlay, (byte) 0x7F);

        }
        else {
            isPlayer = false;

            // set spawn packet
            spawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING); // todo: check if living?
            spawnPacket.getIntegers().write(1, ArenaApi.getInstance().getNmsBridge().entityBridge()
                    .getEntityTypeID(data.getEntityType()));


            // init metadata packet
            WrappedDataWatcher.Serializer customNameSerializer = WrappedDataWatcher.Registry
                    .getChatComponentSerializer(true);
            WrappedDataWatcher.WrappedDataWatcherObject customName
                    = new WrappedDataWatcher.WrappedDataWatcherObject(2, customNameSerializer);
            WrappedDataWatcher.Serializer customNameVisibleSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
            WrappedDataWatcher.WrappedDataWatcherObject customNameVisible
                    = new WrappedDataWatcher.WrappedDataWatcherObject(3, customNameVisibleSerializer);

            wrappedDataWatcher.setObject(customName, Optional.of(WrappedChatComponent.fromText(PLAY_ZOMBIES)
                    .getHandle()));
            wrappedDataWatcher.setObject(customNameVisible, true);

        }

        metadataPacket.getWatchableCollectionModifier()
                .write(0, wrappedDataWatcher.getWatchableObjects());

        // init spawn packet
        spawnPacket.getIntegers().write(0, id);
        spawnPacket.getUUIDs().write(0, uniqueId);
        spawnPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        // init head rotation packet
        headRotationPacket.getIntegers().write(0, id);
        headRotationPacket.getBytes()
                .write(0, (byte) (data.direction * 256.0F / 360.0F));


        // init head look packet
        lookPacket.getIntegers().write(0, id);
        lookPacket.getBytes()
                .write(0, (byte) (data.direction * 256.0F / 360.0F))
                .write(1, (byte) 0);
        lookPacket.getBooleans().write(0, true);


        // init GUI inventory
        List<MapData> mapDataList = Zombies.getInstance().getArenaManager().getMaps();
        int num = mapDataList.size();

        if (num > 0) {
            int width = (int) Math.ceil(Math.sqrt(num));
            int height = (int) Math.ceil((double) num / width);
            int remainderLine = Math.min(6, height) / 2;
            // this is the first line offset
            int offset = (height <= 4) ? 1 : 0;
            // If the height go higher than 6 we need to change our calculation
            if (height > 6) {
                width = (int) Math.ceil((double) num / 6);
            }
            int finalLine = num % width;
            if (finalLine == 0) {
                finalLine = width;
            }

            int guiSize = 9 * Math.min(6, height + 2);
            mapInventory = Bukkit.createInventory(null, guiSize, Component.text(PLAY_ZOMBIES));

            int index = 0;

            for (int h = 0; h < height; h++) {
                int lineCount = (h == remainderLine) ? finalLine : width;
                for (int w = 0; w < lineCount && index < num; w++) {
                    int slot = (18 * w + 9) / (2 * lineCount);
                    int pos = (h + offset) * 9 + slot;

                    MapData mapData = mapDataList.get(index);

                    ItemStack mapDataItemStackRepresentation = new ItemStack(mapData.getItemStackMaterial());
                    mapDataItemStackRepresentation.setLore(mapData.getItemStackLore());

                    ItemMeta itemMeta = mapDataItemStackRepresentation.getItemMeta();
                    itemMeta.displayName(Component.text(mapData.getItemStackDisplayName()));

                    mapDataItemStackRepresentation.setItemMeta(itemMeta);

                    mapInventory.setItem(pos, mapDataItemStackRepresentation);

                    mapNameMap.put(pos, mapData.getName());
                    index++;
                }
            }
        } else {
            mapInventory = Bukkit.createInventory(null, 9, Component.text(PLAY_ZOMBIES));
        }

        rejoinButton = new ItemStack(Material.ENDER_EYE);
        ItemMeta rejoinItemMeta = rejoinButton.getItemMeta();
        rejoinItemMeta.displayName(Component.text(REJOIN_ZOMBIES));
        rejoinButton.setItemMeta(rejoinItemMeta);

        mapInventory.setItem(mapInventory.getSize() - 1, rejoinButton);

        returnButton = new ItemStack(Material.ARROW);
        ItemMeta returnItemMeta = returnButton.getItemMeta();
        returnItemMeta.displayName(Component.text(PLAY_ZOMBIES));
        returnButton.setItemMeta(returnItemMeta);


        // Register listener events
        Bukkit.getServer().getPluginManager().registerEvents(this, Zombies.getInstance());


        // Display to players that are already online
        for (Player player : world.getPlayers()) {
            displayToPlayer(player);
        }
    }

    /**
     * Displays the NPC to a player
     * @param player Thep layer to display the NPC to
     */
    public void displayToPlayer(Player player) {
        Zombies zombies = Zombies.getInstance();
        ArenaApi arenaApi = ArenaApi.getInstance();
        if (isPlayer) {
            arenaApi.sendPacketToPlayer(zombies, player, playerAddPacket);
            Bukkit.getScheduler().runTaskLater(Zombies.getInstance(),
                    () -> arenaApi.sendPacketToPlayer(zombies, player, playerRemovePacket), 40L);
        }
        arenaApi.sendPacketToPlayer(zombies, player, spawnPacket);
        arenaApi.sendPacketToPlayer(zombies, player, metadataPacket);
        arenaApi.sendPacketToPlayer(zombies, player, headRotationPacket);
        arenaApi.sendPacketToPlayer(zombies, player, lookPacket);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().equals(location.getWorld())) {
            displayToPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().equals(location.getWorld())) {
            displayToPlayer(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && humanEntity instanceof Player player) {
            if (clickedInventory.equals(mapInventory)) {
                String mapData = mapNameMap.get(event.getSlot());

                if (mapData != null) {
                    player.closeInventory();

                    ArenaApi arenaApi = Zombies.getInstance().getArenaApi();
                    Joinable joinable = null;
                    PartyPlugin partyPlusPlus = ArenaApi.getInstance().getPartyPlusPlus();
                    if (partyPlusPlus != null) {
                        Optional<Party> partyOptional = partyPlusPlus.getPartyTracker().getPartyForPlayer(player);
                        if (partyOptional.isPresent()) {
                            if (!partyOptional.get().isOwner(player)) {
                                player.sendMessage(Component.text("You are not the owner of the party!",
                                        NamedTextColor.RED));
                                return;
                            }
                            joinable = new SimpleJoinable(partyOptional.get().getOnlinePlayers());
                        }
                    }
                    if (joinable == null) {
                        joinable = new SimpleJoinable(Collections.singletonList(player));
                    }

                    JoinInformation testInformation = new JoinInformation(joinable,
                            Zombies.getInstance().getArenaManager().getGameName(), mapData, null,
                            null);

                    arenaApi.handleJoin(testInformation, (pair) -> {
                        if (!pair.getLeft()) {
                            player.sendMessage(pair.getRight());
                        }
                    });
                } else if (event.getSlot() == mapInventory.getSize() - 1) { // bukkit is weird so sidestep check
                    Inventory rejoinInventory = Bukkit.createInventory(null, 54,
                            Component.text(REJOIN_ZOMBIES));
                    rejoinInventory.setItem(53, returnButton);

                    Set<Map.Entry<UUID, ZombiesArena>> arenas = Zombies.getInstance().getArenaManager()
                            .getArenasWithPlayer(player).entrySet();
                    ItemStack[] itemStacks = new ItemStack[arenas.size()];

                    int counter = 0;
                    for (Map.Entry<UUID, ZombiesArena> arena : arenas) {
                        MapData map = arena.getValue().getMap();

                        ItemStack itemStack = new ItemStack(map.getItemStackMaterial());

                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.displayName(Component.text(map.getItemStackDisplayName()));

                        List<String> lore = new ArrayList<>(map.getItemStackLore());
                        lore.add(arena.getKey().toString());
                        itemMeta.setLore(lore);
                        itemStack.setItemMeta(itemMeta);

                        itemStacks[counter++] = itemStack;
                    }

                    for (int i = 0; i < itemStacks.length; i++) { // TODO: size check
                        rejoinInventory.setItem(i, itemStacks[i]);
                    }

                    player.openInventory(rejoinInventory);
                }
            } else if (event.getView().getTitle().equals(REJOIN_ZOMBIES)) {
                if (event.getSlot() == 53) {
                    player.openInventory(mapInventory);
                } else {
                    ItemStack clicked = event.getCurrentItem();

                    if (clicked != null) {
                        ItemMeta itemMeta = clicked.getItemMeta();

                        if (itemMeta != null) {
                            List<String> lore = itemMeta.getLore();

                            if (lore != null && lore.size() > 0) {
                                String uuidString = lore.get(lore.size() - 1);
                                if (uuidString.matches(UUID_REGEX)) {
                                    UUID uuid = UUID.fromString(uuidString);

                                    if (Zombies.getInstance().getArenaManager().getArenas().containsKey(uuid)) {
                                        ArenaApi arenaApi = Zombies.getInstance().getArenaApi();
                                        Joinable joinable = new SimpleJoinable(Collections.singletonList(player));

                                        JoinInformation testInformation = new JoinInformation(joinable,
                                                Zombies.getInstance().getArenaManager().getGameName(), null,
                                                uuid, null);

                                        arenaApi.handleJoin(testInformation, (pair) -> {
                                            if (!pair.getLeft()) {
                                                player.sendMessage(pair.getRight());
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Destroys the NPC
     */
    public void destroy() {
        // Remove NPC
        PacketContainer killPacketContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        killPacketContainer.getIntegerArrays().write(0, new int[] { id });

        Zombies zombies = Zombies.getInstance();
        ArenaApi arenaApi = ArenaApi.getInstance();
        for (Player player : location.getWorld().getPlayers()) {
            arenaApi.sendPacketToPlayer(zombies, player, killPacketContainer);
        }


        // Stop tracking npc and disable packet listener if necessary
        NPC_MAP.remove(id);
        if (NPC_MAP.size() == 0) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.removePacketListener(packetAdapter);
        }


        HandlerList.unregisterAll(this);
    }

    @RequiredArgsConstructor
    @Getter
    public static class ZombiesNPCData implements ConfigurationSerializable {

        private final EntityType entityType;

        private final Vector location;

        private final float direction;

        private final WrappedSignedProperty texture;

        @Override
        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> serialized = new HashMap<>();
            serialized.put("entityType", entityType.toString());
            serialized.put("location", location);
            serialized.put("direction", direction);

            if (texture != null) {
                serialized.put("textureValue", texture.getValue());
                serialized.put("signature", texture.getSignature());
            }

            return serialized;
        }

        @SuppressWarnings("unused")
        public static ZombiesNPCData deserialize(Map<String, Object> data) {
            EntityType entityType = EntityType.valueOf((String) data.get("entityType"));
            Vector location = (Vector) data.get("location");
            float direction = (float) (double) data.get("direction");

            String textureValue = (String) data.get("textureValue");
            String signature = (String) data.get("signature");

            WrappedSignedProperty texture;
            if (textureValue != null && signature != null) {
                texture = WrappedSignedProperty.fromValues("textures", textureValue, signature);
            } else {
                texture = null;
            }

            return new ZombiesNPCData(entityType, location, direction, texture);
        }

    }

}
