package io.github.zap.zombies.game.corpse;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.player.PlayerBridge;
import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.perk.FastRevive;
import io.github.zap.zombies.game.equipment.perk.Speed;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the corpse of a knocked down or dead player
 */
public class Corpse {

    private final PlayerBridge playerBridge;

    @Getter
    private final ZombiesPlayer zombiesPlayer;

    private final ItemStack boots, leggings, chestplate, helmet;

    @Getter
    private final Location location;

    private final UUID uniqueId = UUID.randomUUID();

    private final int id;

    private final Hologram hologram;

    private final int defaultDeathTime;

    private int deathTaskId;

    private int deathTime;

    @Getter
    private boolean active = true;

    private ZombiesPlayer reviver;

    private int reviveTime;

    public Corpse(@NotNull ZombiesPlayer player) {
        this.playerBridge = ArenaApi.getInstance().getNmsBridge().playerBridge();
        this.zombiesPlayer = player;

        if (player.getPlayer() != null) {
            EntityEquipment equipment = player.getPlayer().getEquipment();
            ItemStack boots = equipment.getBoots(), leggings = equipment.getLeggings();
            ItemStack chestplate = equipment.getChestplate(), helmet = equipment.getHelmet();
            this.boots = (boots != null) ? new ItemStack(boots.getType()) : null;
            this.leggings = (leggings != null) ? new ItemStack(leggings.getType()) : null;
            this.chestplate = (chestplate != null) ? new ItemStack(chestplate.getType()) : null;
            this.helmet = (helmet != null) ? new ItemStack(helmet.getType()) : null;

            EntityBridge entityBridge = ArenaApi.getInstance().getNmsBridge().entityBridge();

            this.location = player.getPlayer().getLocation();
            this.defaultDeathTime = player.getArena().getMap().getCorpseDeathTime();
            this.hologram = new Hologram(location.clone().add(0, 1, 0));
            this.deathTime = defaultDeathTime;
            this.id = entityBridge.nextEntityID();

            hologram.addLine(Component.text("----------------------------------", NamedTextColor.YELLOW));
            hologram.addLine(Component.text("REVIVE THIS PLAYER", NamedTextColor.RED));

            hologram.addLine(Component.text(TimeUtil.convertTicksToSecondsString(defaultDeathTime),
                    NamedTextColor.RED));
            hologram.addLine(Component.text("----------------------------------", NamedTextColor.YELLOW));

            ZombiesArena zombiesArena = player.getArena();
            zombiesArena.getCorpses().add(this);
            zombiesArena.getAvailableCorpses().add(this);
            zombiesArena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
            zombiesArena.getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);

            spawnDeadBody();
            startDying();
        }
        else {
            throw new IllegalArgumentException("Tried to construct a corpse for a player that does not exist!");
        }
    }

    /**
     * Terminates the corpse's execution.
     */
    public void terminate() {
        if (hologram.getHologramLines().size() > 0) {
            hologram.destroy();
        }

        ZombiesArena zombiesArena = zombiesPlayer.getArena();
        zombiesArena.getAvailableCorpses().remove(this);

        if (deathTaskId != -1) {
            Bukkit.getScheduler().cancelTask(deathTaskId);
        }

        active = false;
    }

    /**
     * Sets the current reviver of the corpse
     * @param reviver The reviver of the corpse
     */
    public void setReviver(ZombiesPlayer reviver) {
        if (active) {
            if (reviver == null) {
                zombiesPlayer.getArena().getAvailableCorpses().add(this);
                startDying();
            } else {
                zombiesPlayer.getArena().getAvailableCorpses().remove(this);
                if (deathTaskId != -1) {
                    Bukkit.getScheduler().cancelTask(deathTaskId);
                }

                boolean anyFastRevive = false;
                HotbarManager hotbarManager = reviver.getHotbarManager();
                HotbarObjectGroup hotbarObjectGroup = hotbarManager
                        .getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());

                if (hotbarObjectGroup != null) {
                    for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof FastRevive fastRevive) {
                            reviveTime = Math.max(reviver.getArena().getMap()
                                    .getDefaultReviveTime() - fastRevive.getReducedReviveTime(), 0);

                            anyFastRevive = true;

                            break;
                        }
                    }
                }

                if (!anyFastRevive) {
                    reviveTime = reviver.getArena().getMap().getDefaultReviveTime();
                }

                hologram.updateLine(1, Component.text("Reviving...", NamedTextColor.RED));
            }

            this.reviver = reviver;
        }
    }

    /**
     * Removes 0.1s of revival time from the corpse
     */
    public void continueReviving() {
        if (active) {
            if (reviveTime <= 0) {
                zombiesPlayer.revive();

                Player thisPlayer = zombiesPlayer.getPlayer();
                Player reviverPlayer = reviver.getPlayer();

                if (thisPlayer != null && reviverPlayer != null) {
                    thisPlayer.sendActionBar(Component.empty());
                    reviverPlayer.sendActionBar(Component.empty());

                    ZombiesArena arena = reviver.getArena();
                    MapData map = arena.getMap();
                    PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, map.getReviveSpeedTicks(),
                            map.getReviveSpeedLevel(), true, false, false);
                    reviverPlayer.addPotionEffect(speedEffect);

                    arena.getStatsManager().queueCacheRequest(CacheInformation.PLAYER,
                            reviverPlayer.getUniqueId(), PlayerGeneralStats::new, (stats) -> {
                        PlayerMapStats mapStats = stats.getMapStatsForMap(arena.getMap());
                        mapStats.setPlayersRevived(mapStats.getPlayersRevived() + 1);
                    });

                    HotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                    HotbarObjectGroup hotbarObjectGroup = hotbarManager
                            .getHotbarObjectGroup(EquipmentObjectGroupType.PERK.name());

                    if (hotbarObjectGroup != null) {
                        for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
                            if (hotbarObject instanceof Speed speed) {
                                speed.activate();
                                break;
                            }
                        }
                    }

                    Component message = TextComponent.ofChildren(
                            thisPlayer.displayName(),
                            Component.text(" was revived by ", NamedTextColor.RED),
                            reviverPlayer.displayName(),
                            Component.text("!", NamedTextColor.RED)
                    );
                    for (Player player : arena.getWorld().getPlayers()) {
                        player.sendMessage(message);
                    }
                }

                destroy();
            } else {
                String timeRemaining = TimeUtil.convertTicksToSecondsString(reviveTime);
                Component timeRemainingComponent = Component.text(timeRemaining, NamedTextColor.RED);
                hologram.updateLine(2, timeRemainingComponent);

                Player bukkitPlayer = zombiesPlayer.getPlayer();
                Player reviverPlayer = reviver.getPlayer();

                if (bukkitPlayer != null && reviverPlayer != null) {
                    bukkitPlayer.sendActionBar(TextComponent.ofChildren(
                            Component.text("You are being revived by ",
                                    NamedTextColor.RED),
                            Component.text(reviverPlayer.getName(), NamedTextColor.YELLOW),
                            Component.text("!", NamedTextColor.RED),
                            Component.text(" - ", NamedTextColor.WHITE),
                            timeRemainingComponent,
                            Component.text("!", NamedTextColor.RED)));
                    reviverPlayer.sendActionBar(TextComponent.ofChildren(
                            Component.text("Reviving ", NamedTextColor.RED),
                            Component.text(bukkitPlayer.getName(), NamedTextColor.YELLOW),
                            Component.text("...", NamedTextColor.YELLOW),
                            Component.text(" - ", NamedTextColor.WHITE),
                            timeRemainingComponent,
                            Component.text("!", NamedTextColor.RED)));

                    reviveTime -= 2;
                }
            }
        }
    }

    /**
     * Adds the corpse to a scoreboard team for nametag invisibility
     * @param player The player to send the packet to
     */
    public void addCorpseToScoreboardTeamForPlayer(Player player) {
        PacketContainer addCorpseToTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        addCorpseToTeamPacket.getStrings().write(0, zombiesPlayer.getArena().getCorpseTeamName());
        addCorpseToTeamPacket.getIntegers().write(0, 3);
        addCorpseToTeamPacket.getSpecificModifier(Collection.class)
                .write(0, Collections.singletonList(uniqueId.toString().substring(0, 16)));

        if(player != null) {
            sendPacketToPlayer(addCorpseToTeamPacket, player);
        }
    }

    private void startDying() {
        deathTime = defaultDeathTime;
        hologram.updateLine(1, Component.text("REVIVE THIS PLAYER", NamedTextColor.RED));

        deathTaskId = zombiesPlayer.getArena().runTaskTimer(0, 2, this::continueDying).getTaskId();
    }

    private void continueDying() {
        if (deathTime <= 0) {
            zombiesPlayer.kill("DEFAULT");

            Player bukkitPlayer = zombiesPlayer.getPlayer();
            if (bukkitPlayer != null) {
                bukkitPlayer.sendActionBar(Component.empty());
            }

            active = false;
        } else {
            String timeRemaining = TimeUtil.convertTicksToSecondsString(deathTime);
            hologram.updateLine(2, Component.text(timeRemaining, NamedTextColor.RED));

            Player bukkitPlayer = zombiesPlayer.getPlayer();
            if(bukkitPlayer != null) {
                bukkitPlayer.sendActionBar(TextComponent.ofChildren(
                        Component.text("You will die in " + timeRemaining,
                                NamedTextColor.RED),
                        Component.text("!", NamedTextColor.YELLOW)));
            }
            deathTime -= 2;
        }
    }

    private void onPlayerJoin(ManagingArena.PlayerListArgs playerListArgs) {
        for (Player player : playerListArgs.getPlayers()) {
            spawnDeadBodyForPlayer(player);
            hologram.renderToPlayer(player);
        }
    }

    private void onPlayerRejoin(ZombiesArena.ManagedPlayerListArgs playerListArgs) {
        for (ZombiesPlayer player : playerListArgs.getPlayers()) {
            spawnDeadBodyForPlayer(zombiesPlayer.getPlayer());
            hologram.renderToPlayer(zombiesPlayer.getPlayer());
        }
    }

    private void sendPacketToPlayer(PacketContainer packetContainer, Player player) {
        ArenaApi.getInstance().sendPacketToPlayer(Zombies.getInstance(), player, packetContainer);
    }

    private void sendPacket(PacketContainer packetContainer) {
        Player bukkitPlayer = zombiesPlayer.getPlayer();

        if(bukkitPlayer != null) {
            for (Player player : bukkitPlayer.getWorld().getPlayers()) {
                sendPacketToPlayer(packetContainer, player);
            }
        }
    }

    private void spawnDeadBody() {
        Player bukkitPlayer = zombiesPlayer.getPlayer();

        if(bukkitPlayer != null) {
            for (Player player : bukkitPlayer.getWorld().getPlayers()) {
                spawnDeadBodyForPlayer(player);
            }
        }
    }

    private void spawnDeadBodyForPlayer(Player player) {
        sendPacketToPlayer(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.ADD_PLAYER), player);
        sendPacketToPlayer(createSpawnPlayerPacketContainer(), player);
        sendPacketToPlayer(createArmorPacketContainer(), player);
        sendPacketToPlayer(createSleepingPacketContainer(), player);
        addCorpseToScoreboardTeamForPlayer(player);

        zombiesPlayer.getArena().runTaskLater(1L,
                () -> sendPacketToPlayer(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER),
                        player));
    }

    private PacketContainer createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction playerInfoAction) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packetContainer.getPlayerInfoAction().write(0, playerInfoAction);

        WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uniqueId, uniqueId.toString().substring(0, 16));

        Player player = zombiesPlayer.getPlayer();
        if (player != null) {
            WrappedSignedProperty skin = playerBridge.getSkin(zombiesPlayer.getPlayer());
            if (skin != null) {
                wrappedGameProfile.getProperties().put("textures", skin);
            }
        }

        List<PlayerInfoData> list = new ArrayList<>();
        list.add(new PlayerInfoData(wrappedGameProfile, 0, EnumWrappers.NativeGameMode.NOT_SET,
                WrappedChatComponent.fromText(uniqueId.toString().substring(0, 16))));
        packetContainer.getPlayerInfoDataLists().write(0, list);
        return packetContainer;
    }



    private PacketContainer createSpawnPlayerPacketContainer() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getUUIDs().write(0, uniqueId);

        Player bukkitPlayer = zombiesPlayer.getPlayer();

        if(bukkitPlayer != null) {
            Location location = zombiesPlayer.getPlayer().getLocation();
            packetContainer.getDoubles()
                    .write(0, location.getX())
                    .write(1, location.getY())
                    .write(2, location.getZ());

            return packetContainer;
        }

        throw new IllegalArgumentException("Tried to send packet container for player that does not exist!");
    }

    private @NotNull PacketContainer createArmorPacketContainer() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, id);

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentSlotStackPairList = new ArrayList<>();
        equipmentSlotStackPairList.add(new Pair<>(EnumWrappers.ItemSlot.FEET, boots));
        equipmentSlotStackPairList.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, leggings));
        equipmentSlotStackPairList.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, chestplate));
        equipmentSlotStackPairList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, helmet));

        packetContainer.getSlotStackPairLists().write(0, equipmentSlotStackPairList);

        return packetContainer;
    }

    private PacketContainer createSleepingPacketContainer() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, id);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        Object nmsPose = EnumWrappers.EntityPose.SLEEPING.toNms();
        WrappedDataWatcher.Serializer poseSerializer = WrappedDataWatcher.Registry.get(nmsPose.getClass());
        WrappedDataWatcher.WrappedDataWatcherObject pose
                = new WrappedDataWatcher.WrappedDataWatcherObject(6, poseSerializer);

        WrappedDataWatcher.Serializer overlaySerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject overlay
                = new WrappedDataWatcher.WrappedDataWatcherObject(16, overlaySerializer);


        wrappedDataWatcher.setObject(pose, nmsPose);
        wrappedDataWatcher.setObject(overlay, (byte) 0x7F);

        packetContainer.getWatchableCollectionModifier().write(0, wrappedDataWatcher.getWatchableObjects());

        return packetContainer;
    }

    /**
     * Destroys the corpse and removes its trace from the arena it is in
     */
    public void destroy() {
        PacketContainer removeCorpseFromTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        removeCorpseFromTeamPacket.getStrings().write(0, zombiesPlayer.getArena().getCorpseTeamName());
        removeCorpseFromTeamPacket.getIntegers().write(0, 4);
        removeCorpseFromTeamPacket.getSpecificModifier(Collection.class)
                .write(0, Collections.singletonList(uniqueId.toString().substring(0, 16)));

        sendPacket(removeCorpseFromTeamPacket);

        PacketContainer killPacketContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        killPacketContainer.getIntegerArrays().write(0, new int[]{id});

        sendPacket(killPacketContainer);

        ZombiesArena zombiesArena = getZombiesPlayer().getArena();
        terminate();
        zombiesArena.getCorpses().remove(this);
        zombiesArena.getPlayerJoinEvent().removeHandler(this::onPlayerJoin);
        zombiesArena.getPlayerRejoinEvent().removeHandler(this::onPlayerRejoin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Corpse corpse = (Corpse) o;

        return uniqueId.equals(corpse.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
