package io.github.zap.zombies.game;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.DisposableBukkitRunnable;
import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.chunk.ChunkCoordinateProviders;
import io.github.zap.arenaapi.pathfind.util.Utils;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.commons.utils.MetadataHelper;
import io.github.zap.commons.vectors.Vectors;
import io.github.zap.zombies.MetadataKeys;
import io.github.zap.zombies.SpawnMetadata;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.corpse.Corpse;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.*;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import io.github.zap.zombies.game.data.map.shop.ShopManager;
import io.github.zap.zombies.game.data.powerups.DamageModificationPowerUpData;
import io.github.zap.zombies.game.equipment.melee.MeleeWeapon;
import io.github.zap.zombies.game.hotbar.ZombiesHotbarManager;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.powerups.DamageModificationPowerUp;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpBossBar;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import io.github.zap.zombies.game.powerups.managers.PowerUpManager;
import io.github.zap.zombies.game.powerups.spawnrules.DefaultPowerUpSpawnRule;
import io.github.zap.zombies.game.powerups.spawnrules.PowerUpSpawnRule;
import io.github.zap.zombies.game.scoreboards.GameScoreboard;
import io.github.zap.zombies.game.shop.*;
import io.github.zap.zombies.leaderboard.Leaderboard;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.map.MapStats;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Getter;
import lombok.Value;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Encapsulates an active Zombies game and handles most related logic.
 */
public class ZombiesArena extends ManagingArena<ZombiesArena, ZombiesPlayer> {
    public interface Spawner {
        /**
         * Spawns the provided SpawnEntries in this arena.
         * @param mobs The mobs to spawn
         * @param method The SpawnMethod to use
         * @param slaSquared The SLA to use
         * @param randomize Whether or not spawnpoints are randomized
         * @param updateCount Whether or not to update the total mob count
         * @return The ActiveMobs that were spawned
         */
        List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method, double slaSquared,
                                  boolean randomize, boolean updateCount);

        /**
         * Spawns the provided SpawnEntries in this arena. Only spawnpoints that match the provided predicate can spawn
         * mobs.
         * @param mobs The mobs to spawn
         * @param method The method to use while spawning
         * @param spawnpointPredicate The predicate used to additionally filter spawnpoints
         * @param slaSquared The value to use for SLA
         * @param randomize Whether or not spawnpoints are shuffled
         * @param updateCount Whether or not to update the total mob count
         * @return The ActiveMobs that were spawned as a result of this operation
         */
        List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                 @NotNull Predicate<SpawnpointData> spawnpointPredicate, double slaSquared,
                                  boolean randomize, boolean updateCount);

        /**
         * Spawns a mob at the specified vector, without performing range checks. If the mob is spawned inside of a
         * window, it will have the appropriate metadata set.
         * @param mobType The mob type to spawn
         * @param vector The vector to spawn the mob at
         * @return The mob that was spawned, or null if it failed to spawn
         */
        ActiveMob spawnMobAt(@NotNull String mobType, @NotNull Vector vector, boolean updateCount);
    }

    /**
     * General interface for an implementation that handles damaging all entities.
     */
    public interface DamageHandler {
        /**
         * Damages an entity.
         * @param target The ActiveMob to damage
         */
        void damageEntity(@NotNull Damager comesFrom, @NotNull DamageAttempt with, @NotNull Mob target);
    }

    /**
     * Basic spawner implementation.
     */
    public class BasicSpawner implements Spawner {
        @Value
        private class SpawnContext {
            SpawnpointData spawnpoint;
            WindowData window;
        }

        @Override
        public List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                         double slaSquared, boolean randomize, boolean updateCount) {
            return spawnMobs(mobs, method, spawnpointData -> true, slaSquared, randomize, updateCount);
        }

        @Override
        public ActiveMob spawnMobAt(@NotNull String mobType, @NotNull Vector vector, boolean updateCount) {
            RoomData roomIn = map.roomAt(vector);
            if(roomIn != null) {
                for(WindowData windowData : roomIn.getWindows()) {
                    if(windowData.vectorInside(vector)) {
                        ActiveMob spawned = spawnMob(mobType, vector, windowData);

                        if(spawned != null) {
                            if(updateCount) {
                                zombiesLeft++;
                            }

                            return spawned;
                        }
                    }
                }
            }

            ActiveMob mob = spawnMob(mobType, vector, null);
            if(mob != null && updateCount) {
                zombiesLeft++;
            }

            return mob;
        }

        @Override
        public List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                         @NotNull Predicate<SpawnpointData> filter, double slaSquared,
                                         boolean randomize, boolean updateCount) {
            List<SpawnContext> spawns = filterSpawnpoints(mobs, method, filter, slaSquared);
            List<ActiveMob> spawnedEntities = new ArrayList<>();

            if(spawns.size() == 0) {
                Zombies.warning("There are no available spawnpoints for this mob set. This likely indicates an error " +
                        "in map configuration.");
                return Collections.emptyList();
            }

            if(randomize) {
                Collections.shuffle(spawns); //shuffle small candidate set of spawnpoints
            }

            for(SpawnEntryData spawnEntryData : mobs) {
                int amt = spawnEntryData.getMobCount();

                outer:
                while(true) {
                    int startAmt = amt;
                    for(SpawnContext context : spawns) {
                        if(method == SpawnMethod.IGNORE_SPAWNRULE || context.spawnpoint.canSpawn(spawnEntryData
                                .getMobName(), map)) {
                            ActiveMob mob = spawnMob(spawnEntryData.getMobName(), context.spawnpoint.getSpawn(), context.window);

                            if(mob != null) {
                                spawnedEntities.add(mob);

                                if(updateCount) {
                                    zombiesLeft++;
                                }
                            }
                            else {
                                //mob failed to spawn; if we're part of a wave, reduce the amt of zombies
                                if(!updateCount) {
                                    zombiesLeft--;
                                    checkNextRound();
                                }

                                Zombies.warning("Mob failed to spawn!");
                            }

                            if(--amt == 0) {
                                break outer;
                            }
                        }
                    }

                    if(startAmt == amt) { //make sure we managed to spawn at least one mob
                        Zombies.warning("Unable to find a valid spawnpoint for SpawnEntryData.");

                        if(!updateCount) { //reduce zombie count if none spawned due to no windows being in range
                            zombiesLeft -= amt;
                        }
                        break;
                    }
                }
            }

            return spawnedEntities;
        }

        private ActiveMob spawnMob(String mobName, Vector blockPosition, WindowData windowData) {
            MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(mobName);

            if(mob != null) {
                ActiveMob activeMob = mob.spawn(new AbstractLocation(new BukkitWorld(world), blockPosition.getX() +
                        0.5, blockPosition.getY(), blockPosition.getZ() + 0.5), map.getMobSpawnLevel());

                if(activeMob != null) {
                    getEntitySet().add(activeMob.getUniqueId());
                    MetadataHelper.setFixedMetadata(activeMob.getEntity().getBukkitEntity(), Zombies.getInstance(),
                            MetadataKeys.MOB_SPAWN.getKey(), new SpawnMetadata(ZombiesArena.this, windowData));

                    MythicMob type = MythicMobs.inst().getMobManager().getMythicMob(activeMob.getMobType());
                    if(type.getConfig().getBoolean("IsBoss", false)) {
                        RoomData room = map.roomAt(blockPosition);

                        for(ZombiesPlayer player : getPlayerMap().values()) {
                            Player bukkitPlayer = player.getPlayer();
                            if(bukkitPlayer != null && player.isInGame()) {
                                MiniMessage miniMessage = MiniMessage.get();
                                String configDisplayName = type.getConfig().getString("ZombiesDisplayName");
                                Component displayName = configDisplayName != null
                                        ? miniMessage.parse(configDisplayName)
                                        : Component.text(activeMob.getMobType(), NamedTextColor.RED);
                                bukkitPlayer.showTitle(Title.title(
                                        displayName,
                                        Component.text("spawned in " + room.getRoomDisplayName(),
                                                TextColor.color(61, 61, 61)),
                                        Title.Times.of(Duration.ofSeconds(1), Duration.ofSeconds(3),
                                                Duration.ofSeconds(1))));
                            }
                        }
                    }

                    return activeMob;
                }
                else {
                    Zombies.warning(String.format("An error occurred while trying to spawn mob of type '%s'.", mobName));
                }
            }
            else {
                Zombies.warning(String.format("Mob type '%s' is not known.", mobName));
            }

            return null;
        }

        private List<SpawnContext> filterSpawnpoints(List<SpawnEntryData> mobs, SpawnMethod method,
                                                     Predicate<SpawnpointData> filter, double slaSquared) {
            List<SpawnContext> filtered = new ArrayList<>();

            for(RoomData room : map.getRooms()) { //iterate rooms
                if(room.isSpawn() || method == SpawnMethod.FORCE || room.getOpenProperty().getValue(ZombiesArena.this)) {
                    //add all valid spawnpoints in the room
                    addValidContext(filtered, room.getSpawnpoints(), mobs, method, filter, slaSquared, null);

                    for(WindowData window : room.getWindows()) { //check window spawnpoints next
                        addValidContext(filtered, window.getSpawnpoints(), mobs, method, filter, slaSquared, window);
                    }
                }
            }

            return filtered;
        }

        private void addValidContext(List<SpawnContext> addTo, List<SpawnpointData> spawnpoints, List<SpawnEntryData> mobs,
                                     SpawnMethod method, Predicate<SpawnpointData> filter,
                                     double slaSquared, WindowData window) {
            for(SpawnpointData spawnpointData : spawnpoints) {
                if(filter.test(spawnpointData) && canSpawnAny(spawnpointData, mobs, method, slaSquared)) {
                    addTo.add(new SpawnContext(spawnpointData, window));
                }
            }
        }

        private boolean canSpawnAny(SpawnpointData spawnpoint, List<SpawnEntryData> entry, SpawnMethod method, double slaSquared) {
            if(method == SpawnMethod.IGNORE_SPAWNRULE) {
                return checkSLA(spawnpoint, slaSquared);
            }
            else {
                for(SpawnEntryData data : entry) {
                    if(spawnpoint.canSpawn(data.getMobName(), map)) {
                        return method != SpawnMethod.RANGED || checkSLA(spawnpoint, slaSquared);
                    }
                }
            }

            return false;
        }

        private boolean checkSLA(SpawnpointData target, double slaSquared) {
            for(ZombiesPlayer player : getPlayerMap().values()) {
                Player bukkitPlayer = player.getPlayer();

                if(bukkitPlayer != null && player.isInGame() && player.isAlive()) {
                    if(bukkitPlayer.getLocation().toVector().distanceSquared(target.getSpawn()) <= slaSquared) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public class BasicDamageHandler implements DamageHandler {
        @Override
        public void damageEntity(@NotNull Damager damager, @NotNull DamageAttempt with, @NotNull Mob target) {
            if (hasEntity(target.getUniqueId()) && !target.isDead()) {
                target.playEffect(EntityEffect.HURT);

                Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(target.getUniqueId());
                double mobKbFactor = 1;
                if(activeMob.isPresent()) {
                    mobKbFactor = activeMob.get().getType().getConfig().getDouble("KnockbackFactor", 1);
                }

                Player player = null;
                if(damager instanceof ZombiesPlayer zp) {
                    player = zp.getPlayer();
                }

                double deltaHealth = inflictDamage(player, target, with.damageAmount(damager, target), with.ignoresArmor(damager, target));
                Vector resultingVelocity = target.getVelocity().add(with.directionVector(damager, target)
                        .multiply(with.knockbackFactor(damager, target)).multiply(mobKbFactor));

                try {
                    target.setVelocity(resultingVelocity);
                }
                catch (IllegalArgumentException ignored) {
                    Zombies.warning("Attempted to set velocity for entity " + target.getUniqueId() + " to a vector " +
                            "with a non-finite value " + resultingVelocity);
                }

                damager.onDealsDamage(with, target, deltaHealth);
            }
        }

        private double inflictDamage(Entity damager, Damageable mob, double damage, boolean ignoreArmor) {
            boolean instaKill = false;

            for(PowerUp powerup : getPowerUps()) {
                if(powerup instanceof DamageModificationPowerUp && powerup.getState() == PowerUpState.ACTIVATED) {
                    var data = (DamageModificationPowerUpData) powerup.getData();
                    if(data.isInstaKill()) {
                        instaKill = true;
                        break;
                    }

                    damage = damage * data.getMultiplier() + data.getAdditionalDamage();
                }
            }

            double before = mob.getHealth();

            Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(mob.getUniqueId());
            boolean resistInstakill = false;
            if(activeMob.isPresent()) {
                resistInstakill = activeMob.get().getType().getConfig().getBoolean("ResistInstakill", false);
            }

            if(instaKill && !resistInstakill) {
                double health = mob.getHealth();
                mob.setHealth(0);

                if(damager != null) {
                    Bukkit.getPluginManager().callEvent(new EntityDamageByEntityEvent(damager, mob,
                            EntityDamageEvent.DamageCause.CUSTOM, health));
                }
            } else if(ignoreArmor) {
                mob.setHealth(Math.max(mob.getHealth() - damage, 0D));

                if(damager != null) {
                    Bukkit.getPluginManager().callEvent(new EntityDamageByEntityEvent(damager, mob,
                            EntityDamageEvent.DamageCause.CUSTOM, damage));
                }
            } else {
                mob.damage(damage, null);
            }

            mob.playEffect(EntityEffect.HURT);
            return before - mob.getHealth();
        }
    }

    private static final String MOB_SPEEDUP_ATTRIBUTE_NAME = "mob_speedup";

    @Getter
    private final MapData map;

    @Getter
    private final EquipmentManager equipmentManager;

    @Getter
    private final PowerUpManager powerUpManager;

    @Getter
    private final ShopManager shopManager;

    @Getter
    private final StatsManager statsManager;

    @Getter
    private final DamageHandler damageHandler;

    @Getter
    private final Spawner spawner;

    @Getter
    protected ZombiesArenaState state = ZombiesArenaState.PREGAME;

    @Getter
    private final Random random = new Random(); // TODO: static?

    @Getter
    private final long emptyTimeout;

    @Getter
    private final List<Shop<?>> shops = new ArrayList<>();

    @Getter
    private final Map<String, List<Shop<?>>> shopMap = new HashMap<>();

    @Getter
    private final Map<String, Event<ShopEventArgs>> shopEvents = new HashMap<>();

    @Getter
    private String luckyChestRoom;

    @Getter
    private String piglinRoom;

    @Getter
    private final String corpseTeamName = UUID.randomUUID().toString().substring(0, 16);

    @Getter
    private final Set<Corpse> corpses = new HashSet<>();

    @Getter
    private final Set<Corpse> availableCorpses = new HashSet<>();
    @Getter
    private final Set<Item> protectedItems = new HashSet<>();

    @Getter
    private final GameScoreboard gameScoreboard;

    @Getter
    private final ChunkBounds mapBounds;

    @Getter
    private final Set<Player> hiddenPlayers = new HashSet<>() {
        @Override
        public boolean add(Player player) {
            if (super.add(player)) {
                for (Player otherPlayer : world.getPlayers()) {
                    otherPlayer.hidePlayer(Zombies.getInstance(), player);
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Player player && super.remove(o)) {
                for (Player otherPlayer : world.getPlayers()) {
                    otherPlayer.showPlayer(Zombies.getInstance(), player);
                }

                return true;
            }

            return false;
        }

    };

    private final Leaderboard timesLeaderboard;

    private final Leaderboard timesLeaderboard2;

    /**
     * Indicate when the game start using System.currentTimeMillis()
     * return -1 if the game hasn't start
     */
    @Getter
    private long startTimeStamp = -1;

    /**
     * Indicate when the game end using System.currentTimeMillis()
     * return -1 if the game hasn't end
     */
    @Getter
    private long endTimeStamp = -1;

    @Getter
    private final Set<Pair<PowerUpSpawnRule<?>, String>> powerUpSpawnRules = new HashSet<>();

    // Contains both active and has not been picked up
    @Getter
    private final Set<PowerUp> powerUps = new HashSet<>();

    @Getter
    private final Event<PowerUpChangedEventArgs> powerUpChangedEvent = new Event<>();

    @Getter
    private final PowerUpBossBar powerUpBossBar = new PowerUpBossBar(this, 5);

    @Getter
    private int zombiesLeft;

    private RoundContext currentRound = null;

    /**
     * Creates a new ZombiesArena with the specified map, world, and timeout.
     *
     * @param map          The map to use
     * @param world        The world to use
     * @param emptyTimeout The time it will take the arena to close, if it is empty and in the pregame state
     */
    public ZombiesArena(ZombiesArenaManager manager, World world, MapData map, @NotNull Leaderboard timesLeaderboard,
                        @Nullable Leaderboard timesLeaderboard2, long emptyTimeout) {
        super(Zombies.getInstance(), manager, world, ZombiesPlayer::new, emptyTimeout);

        this.map = map;
        this.equipmentManager = manager.getEquipmentManager();
        this.powerUpManager = manager.getPowerUpManager();
        this.shopManager = manager.getShopManager();
        this.statsManager = manager.getStatsManager();
        this.emptyTimeout = emptyTimeout;
        this.spawner = new BasicSpawner();
        this.damageHandler = new BasicDamageHandler();
        this.gameScoreboard = new GameScoreboard(this);
        gameScoreboard.initialize();
        this.timesLeaderboard = timesLeaderboard;
        this.timesLeaderboard2 = timesLeaderboard2;

        registerArenaEvents();
        registerDisposables();

        getMap().getPowerUpSpawnRules().forEach(x -> powerUpSpawnRules.add(Pair.of(getPowerUpManager()
                .createSpawnRule(x.getLeft(), x.getRight(), this), x.getRight())));

        BoundingBox bounds = map.getMapBounds();
        Vector min = bounds.getMin();
        Vector max = bounds.getMax();

        mapBounds = ChunkCoordinateProviders.boundedSquare(Vectors.of(min.getBlockX() >> 4, min.getBlockZ() >> 4),
                Vectors.of((max.getBlockX() >> 4) + 1, (max.getBlockZ() >> 4) + 1));
    }

    private void registerArenaEvents() {
        getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);
        getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);

        getProxyFor(PlayerDropItemEvent.class).registerHandler(this::onPlayerDropItem);
        getProxyFor(PlayerMoveEvent.class).registerHandler(this::onPlayerMove);
        getProxyFor(PlayerSwapHandItemsEvent.class).registerHandler(this::onPlayerSwapHandItems);
        getProxyFor(EntityDamageEvent.class).registerHandler(this::onEntityDamaged);
        getProxyFor(EntityDamageByEntityEvent.class).registerHandler(this::onEntityDamageByEntity);
        getProxyFor(EntityDeathEvent.class).registerHandler(this::onEntityDeath);
        getProxyFor(PlayerDeathEvent.class).registerHandler(this::onPlayerDeath);
        getProxyFor(EntityRemoveFromWorldEvent.class).registerHandler(this::onEntityRemoveFromWorldEvent);
        getProxyFor(PlayerInteractEvent.class).registerHandler(this::onPlayerInteract);
        getProxyFor(PlayerInteractAtEntityEvent.class).registerHandler(this::onPlayerInteractAtEntity);
        getProxyFor(BlockPlaceEvent.class).registerHandler(this::onPlaceBlock);
        getProxyFor(BlockBreakEvent.class).registerHandler(this::onBlockBreak);
        getProxyFor(PlayerItemHeldEvent.class).registerHandler(this::onPlayerItemHeld);
        getProxyFor(PlayerItemConsumeEvent.class).registerHandler(this::onPlayerItemConsume);
        getProxyFor(PlayerItemDamageEvent.class).registerHandler(this::onPlayerItemDamage);
        getProxyFor(PlayerAttemptPickupItemEvent.class).registerHandler(this::onPlayerAttemptPickupItem);
        getProxyFor(PlayerArmorStandManipulateEvent.class).registerHandler(this::onPlayerArmorStandManipulate);
        getProxyFor(FoodLevelChangeEvent.class).registerHandler(this::onFoodLevelChange);
        getProxyFor(InventoryClickEvent.class).registerHandler(this::onPlayerInventoryClick);
    }

    private void registerDisposables() {
        ResourceManager resourceManager = getResourceManager();

        resourceManager.addDisposable(gameScoreboard);
        resourceManager.addDisposable(powerUpBossBar);
    }

    @Override
    public ZombiesArena getArena() {
        return this;
    }

    @Override
    public void dispose() {
        for (ZombiesPlayer player : getPlayerMap().values()) {
            if (player.isInGame()) {
                Player bukkitPlayer = player.getPlayer();
                if (bukkitPlayer != null) {
                    for (Player hiddenPlayer : hiddenPlayers) {
                        bukkitPlayer.showPlayer(Zombies.getInstance(), hiddenPlayer);
                    }
                }
            }
        }

        super.dispose(); //dispose of superclass-specific resources

        //cleanup mappings and remove arena from manager
        Property.removeMappingsFor(this);
        manager.unloadArena(getArena());

        EntityAddToWorldEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        ItemDespawnEvent.getHandlerList().unregister(this);
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean allowPlayers() {
        return state != ZombiesArenaState.ENDED && (state != ZombiesArenaState.STARTED || map.isAllowRejoin());
    }

    @Override
    public boolean allowPlayerJoin(List<Player> players) {
        return (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) &&
                getOnlineCount() + players.size() <= map.getMaximumCapacity();
    }

    public boolean allowPlayerRejoin(List<ZombiesPlayer> players) {
        //return state == ZombiesArenaState.STARTED && map.isAllowRejoin();
        return false;
    }

    private void onPlayerJoin(PlayerListArgs args) {
        if(state == ZombiesArenaState.PREGAME && getOnlineCount() >= map.getMinimumCapacity()) {
            state = ZombiesArenaState.COUNTDOWN;
        }

        if (state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) {
            for (Player player : args.getPlayers()) {
                player.teleport(WorldUtils.locationFrom(world, map.getSpawn()));

                player.showTitle(Title.title(Component.text("ZOMBIES", NamedTextColor.YELLOW),
                        map.getSplashScreenSubtitles().isEmpty()
                                ? Component.empty()
                                : Component.text(map.getSplashScreenSubtitles()
                                .get(random.nextInt(map.getSplashScreenSubtitles().size())))));
            }
            if (startTimeStamp == -1) {
                for (Player player : args.getPlayers()) {
                    timesLeaderboard.displayToPlayer(player);
                    if (timesLeaderboard2 != null) {
                        timesLeaderboard2.displayToPlayer(player);
                    }
                }
            }
        }


        if (state == ZombiesArenaState.STARTED || state == ZombiesArenaState.ENDED) {
            for (Player player : args.getPlayers()) {
                for (Player hiddenPlayer : hiddenPlayers) {
                    player.hidePlayer(Zombies.getInstance(), hiddenPlayer);
                }
                if (hiddenPlayers.contains(player)) {
                    for (Player otherPlayer : world.getPlayers()) {
                        otherPlayer.hidePlayer(Zombies.getInstance(), player);
                    }
                }
            }
        }
    }

    private void onPlayerRejoin(ManagedPlayerListArgs args) {
        for (ZombiesPlayer player : args.getPlayers()) {
            Player bukkitPlayer = player.getPlayer();

            if (bukkitPlayer != null) {
                bukkitPlayer.teleport(WorldUtils.locationFrom(world, map.getSpawn()));

                for (Player hiddenPlayer : hiddenPlayers) {
                    bukkitPlayer.hidePlayer(Zombies.getInstance(), hiddenPlayer);
                }
                if (hiddenPlayers.contains(bukkitPlayer)) {
                    for (Player otherPlayer : world.getPlayers()) {
                        otherPlayer.hidePlayer(Zombies.getInstance(), bukkitPlayer);
                    }
                }
            }
        }
    }

    private void onPlayerLeave(ManagedPlayerListArgs args) {
        for(ZombiesPlayer player : args.getPlayers()) { //quit has already been called for these players
            if(!map.isAllowRejoin()) {
                super.removePlayer(player);
            }

            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer != null) {
                for (Player hiddenPlayer : hiddenPlayers) {
                    bukkitPlayer.showPlayer(Zombies.getInstance(), hiddenPlayer);
                }
                if (hiddenPlayers.contains(bukkitPlayer)) {
                    for (Player otherPlayer : world.getPlayers()) {
                        otherPlayer.showPlayer(Zombies.getInstance(), bukkitPlayer);
                    }
                }
            }
        }

        stateLabel:
        switch (state) {
            case PREGAME:
                removePlayers(args.getPlayers());
                break;
            case COUNTDOWN:
                removePlayers(args.getPlayers());

                if (getOnlineCount() < map.getMinimumCapacity()) {
                    state = ZombiesArenaState.PREGAME;
                }
                break;
            case STARTED:
                if (getOnlineCount() == 0) {
                    state = ZombiesArenaState.ENDED;
                    dispose(); //shut everything down immediately if everyone leaves mid-game
                } else {
                    for (ZombiesPlayer player : getPlayerMap().values()) {
                        if (!args.getPlayers().contains(player) && player.isAlive()) {
                            break stateLabel;
                        }
                    }

                    // There are no players alive, so end the game
                    for (ZombiesPlayer player : getPlayerMap().values()) {
                        if (player.isInGame()) {
                            player.kill("DEFAULT");
                        }
                    }
                    doLoss();
                }
                break;
        }
    }

    private void checkNextRound() {
        if(zombiesLeft == 0 && state == ZombiesArenaState.STARTED) {
            Property<Integer> currentRound = map.getCurrentRoundProperty();
            doRound(currentRound.getValue(this) + 1);
        }
    }

    private void onEntityRemoveFromWorldEvent(ProxyArgs<EntityRemoveFromWorldEvent> event) {
        Entity entity = event.getEvent().getEntity();
        if (state == ZombiesArenaState.STARTED && getEntitySet().remove(entity.getUniqueId())) {
            zombiesLeft--;
            entity.removeMetadata(MetadataKeys.MOB_WAVE.getKey(), getPlugin());
            entity.removeMetadata(MetadataKeys.MOB_SPAWN.getKey(), getPlugin());
            checkNextRound();
        }
    }

    @org.bukkit.event.EventHandler
    private void onEntityAddToWorldEvent(EntityAddToWorldEvent event) {
        //only necessary so long as MythicMobs refuses to work right
        Entity entity = event.getEntity();
        if(entity.getType() == EntityType.CHICKEN) {
            if(!getEntitySet().contains(entity.getUniqueId())) {
                for(Entity passenger : entity.getPassengers()) {
                    entity.removePassenger(passenger);
                }

                entity.remove();
            }
        }
    }

    private void onEntityDamaged(ProxyArgs<EntityDamageEvent> args) {
        EntityDamageEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if (managedPlayer != null) {
            Player player = managedPlayer.getPlayer();
            ZombiesPlayerState state = managedPlayer.getState();

            if (player != null) {
                if (state == ZombiesPlayerState.ALIVE) {
                    if (player.getHealth() <= event.getFinalDamage()) {
                        Location location = player.getLocation();

                        BlockCollisionView collisionView = Utils.highestBlockBelow((x, y, z) -> ArenaApi.getInstance()
                                        .getNmsBridge().worldBridge().collisionFor(world.getBlockAt(x, y, z)),
                                player.getBoundingBox());
                        location.setY(collisionView.exactY());

                        player.teleportAsync(location);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
        else if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Entity entity = args.getEvent().getEntity();
            if (getEntitySet().contains(entity.getUniqueId())) {
                entity.remove();
            }
        }
    }

    @org.bukkit.event.EventHandler
    private void onNonManagedEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity.getWorld().equals(world) && entity instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }

    private void onEntityDamageByEntity(ProxyArgs<EntityDamageByEntityEvent> args) {
        EntityDamageByEntityEvent event = args.getEvent();

        if(event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
            Entity entity = event.getEntity(), damager = event.getDamager();
            ZombiesPlayer damagingPlayer = getPlayerMap().get(damager.getUniqueId());

            if (damagingPlayer != null && entity instanceof Mob mob) {
                Player bukkitPlayer = damagingPlayer.getPlayer();

                if (damagingPlayer.isAlive() && bukkitPlayer != null && getEntitySet().contains(mob.getUniqueId())) {
                    HotbarManager hotbarManager = damagingPlayer.getHotbarManager();
                    HotbarObject hotbarObject = hotbarManager.getSelectedObject();

                    if (hotbarObject instanceof MeleeWeapon<?, ?> meleeWeapon) {
                        if (meleeWeapon.isUsable() &&
                                (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                                        || meleeWeapon.getCurrentLevel().isShouldSweep())) {
                            event.setDamage(0D);
                            meleeWeapon.attack(mob);
                        }
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    private void onEntityDeath(ProxyArgs<EntityDeathEvent> args) {
        Entity entity = args.getEvent().getEntity();
        if (state == ZombiesArenaState.STARTED && getEntitySet().remove(entity.getUniqueId())) {
            zombiesLeft--;

            // TODO: THIS IS A HACK NEEDS TO BE FIXED
            for (Pair<PowerUpSpawnRule<?>, String> spawnRule : powerUpSpawnRules) {
                if (spawnRule.getLeft() instanceof DefaultPowerUpSpawnRule defaultPowerUpSpawnRule) {
                    defaultPowerUpSpawnRule.onMobDeath(args);
                }
            }
            // THIS IS A HACK NEEDS TO BE FIXED

            checkNextRound();
        }
    }

    @org.bukkit.event.EventHandler
    private void onItemDespawn(ItemDespawnEvent event) {
        if (protectedItems.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private void onPlayerDeath(ProxyArgs<PlayerDeathEvent> args) {
        args.getEvent().setCancelled(true); //cancel death event

        if (state == ZombiesArenaState.STARTED) {
            ZombiesPlayer knocked = args.getManagedPlayer();

            if (knocked != null && knocked.getState() == ZombiesPlayerState.ALIVE) {
                Player bukkitKnocked = knocked.getPlayer();

                if (bukkitKnocked != null) {
                    knocked.knock();

                    for (ZombiesPlayer player : getPlayerMap().values()) {
                        if (player.isInGame() && player.isAlive()) {
                            Player bukkitPlayer = player.getPlayer();
                            if (bukkitPlayer != null) {
                                RoomData roomIn = map.roomAt(bukkitKnocked.getLocation().toVector());
                                String message = roomIn == null ? "an unknown room" : roomIn.getRoomDisplayName();

                                //display death message only if necessary
                                for (ZombiesPlayer otherPlayer : getPlayerMap().values()) {
                                    if (otherPlayer.isInGame()) {
                                        Player otherBukkitPlayer = otherPlayer.getPlayer();

                                        if (otherBukkitPlayer != null) {
                                            otherBukkitPlayer.showTitle(Title.title(
                                                    Component.text(knocked.getPlayer().getName(), NamedTextColor.YELLOW),
                                                    Component.text("was knocked down in " + message,
                                                            TextColor.color(61, 61, 61)),
                                                    Title.Times.of(Duration.ofSeconds(1), Duration.ofSeconds(3),
                                                            Duration.ofSeconds(1))));

                                            otherPlayer.getPlayer().playSound(Sound.sound(
                                                    Key.key("minecraft:entity.ender_dragon.growl"),
                                                    Sound.Source.MASTER,
                                                    1.0F,
                                                    0.5F
                                            ));

                                            otherBukkitPlayer.sendMessage(Component.text(knocked.getPlayer().getName() +
                                                            " was knocked down in", NamedTextColor.YELLOW)
                                                    .append(Component.text(" " + message, NamedTextColor.DARK_RED)));
                                        }
                                    }
                                }

                                return; //return if there are any players still alive
                            }
                        }
                    }

                    // There are no players alive, so end the game
                    for (ZombiesPlayer player : getPlayerMap().values()) {
                        if (player.isInGame()) {
                            player.kill("DEFAULT");
                        }
                    }
                    doLoss();
                }
            }
        }
    }

    private void onPlayerInteract(ProxyArgs<PlayerInteractEvent> args) {
        PlayerInteractEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();
        Action action = args.getEvent().getAction();

        if(player != null && event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
            boolean noInteractions = true;
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                for (Shop<?> shop : shops) {
                    if (shop.interact(args)) {
                        noInteractions = false;
                        break;
                    }
                }
            }
            if (noInteractions) {
                player.getHotbarManager().click(event.getAction());
            }
        }

        event.setCancelled(true);
    }

    @org.bukkit.event.EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity.getWorld().equals(world) && entity instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }

    private void onPlayerInteractAtEntity(ProxyArgs<PlayerInteractAtEntityEvent> args) {
        PlayerInteractAtEntityEvent event = args.getEvent();
        ZombiesPlayer player = args.getManagedPlayer();

        if (player != null && event.getHand() == EquipmentSlot.HAND && player.isAlive()) {
            boolean noInteractions = true;
            for (Shop<?> shop : shops) {
                if (shop.interact(args)) {
                    noInteractions = false;
                    break;
                }
            }
            if (noInteractions) {
                player.getHotbarManager().click(Action.RIGHT_CLICK_BLOCK);
            }
        }
    }

    private void onPlayerSwapHandItems(ProxyArgs<PlayerSwapHandItemsEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerDropItem(ProxyArgs<PlayerDropItemEvent> args) {
        PlayerDropItemEvent event = args.getEvent();
        ItemStack playerStack = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);

        if (playerStack == null) {
            event.setCancelled(true);
        } else {
            Item dropped = event.getItemDrop();
            dropped.remove();

            if (playerStack.getType() != Material.AIR) {
                playerStack.setAmount(playerStack.getAmount() + 1);
            } else {
                playerStack.setType(dropped.getItemStack().getType());
                event.setCancelled(true);
            }

            event.getPlayer().updateInventory();
        }
    }

    private void onPlayerMove(ProxyArgs<PlayerMoveEvent> args) {
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(managedPlayer != null && managedPlayer.getState() == ZombiesPlayerState.KNOCKED) {
            //disgusting! but works (allows head rotation but not movement)
            if(!args.getEvent().getFrom().toVector().equals(args.getEvent().getTo().toVector())) {
                args.getEvent().setCancelled(true);
            }
        }
    }

    private void onPlayerItemHeld(ProxyArgs<PlayerItemHeldEvent> args) {
        PlayerItemHeldEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if(managedPlayer != null) {
            managedPlayer.getHotbarManager().setSelectedSlot(event.getNewSlot());
        }
    }

    private void onPlaceBlock(ProxyArgs<BlockPlaceEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onBlockBreak(ProxyArgs<BlockBreakEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerItemConsume(ProxyArgs<PlayerItemConsumeEvent> args) {
        args.getEvent().setCancelled(true); // TODO: might need to change this one day
    }

    private void onPlayerItemDamage(ProxyArgs<PlayerItemDamageEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerAttemptPickupItem(ProxyArgs<PlayerAttemptPickupItemEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onPlayerArmorStandManipulate(ProxyArgs<PlayerArmorStandManipulateEvent> args) {
        args.getEvent().setCancelled(true);
    }

    private void onFoodLevelChange(ProxyArgs<FoodLevelChangeEvent> args) {
        FoodLevelChangeEvent event = args.getEvent();
        event.setCancelled(true);
    }

    private void onPlayerInventoryClick(ProxyArgs<InventoryClickEvent> args) {
        InventoryClickEvent event = args.getEvent();
        ZombiesPlayer managedPlayer = args.getManagedPlayer();

        if (managedPlayer != null) {
            Player player = managedPlayer.getPlayer();

            if (player != null && player.getInventory().equals(event.getClickedInventory())) {
                event.setCancelled(true);
            }
        }
    }

    public void startGame() {
        if(state == ZombiesArenaState.PREGAME || state == ZombiesArenaState.COUNTDOWN) {
            loadShops();

            state = ZombiesArenaState.STARTED;
            startTimeStamp = System.currentTimeMillis();

            timesLeaderboard.destroy();
            if (timesLeaderboard2 != null) {
                timesLeaderboard2.destroy();
            }

            for(ZombiesPlayer zombiesPlayer : getPlayerMap().values()) {
                Player bukkitPlayer = zombiesPlayer.getPlayer();

                if(bukkitPlayer != null) {
                    bukkitPlayer.sendMessage(Component.text("Started!", NamedTextColor.YELLOW));
                    zombiesPlayer.setAliveState();

                    Vector spawn = map.getSpawn();
                    zombiesPlayer.getPlayer().teleport(new Location(world, spawn.getX() + 0.5, spawn.getY(),
                            spawn.getZ() + 0.5));

                    ZombiesHotbarManager hotbarManager = zombiesPlayer.getHotbarManager();
                    for (Map.Entry<String, Set<Integer>> hotbarObjectGroupSlot : map
                            .getHotbarObjectGroupSlots().entrySet()) {
                        hotbarManager.addEquipmentObjectGroup(equipmentManager
                                .createEquipmentObjectGroup(hotbarObjectGroupSlot.getKey(), zombiesPlayer.getPlayer(),
                                        hotbarObjectGroupSlot.getValue()));
                    }

                    for(String equipment : map.getDefaultEquipments()) {
                        EquipmentData<?> equipmentData = equipmentManager.getEquipmentData(map.getName(), equipment);

                        if(equipmentData != null) {
                            HotbarObjectGroup hotbarObjectGroup = hotbarManager
                                    .getHotbarObjectGroup(equipmentData.getEquipmentObjectGroupType());

                            if (hotbarObjectGroup != null) {
                                Integer slot = hotbarObjectGroup.getNextEmptySlot();

                                if (slot != null) {
                                    hotbarManager.setHotbarObject(slot, equipmentManager
                                            .createEquipment(this, zombiesPlayer, slot, equipmentData));
                                }
                            }
                        }
                        else {
                            Zombies.warning("Default equipment " + equipment + " does not exist!");
                        }
                    }

                    statsManager.queueCacheRequest(CacheInformation.PLAYER, bukkitPlayer.getUniqueId(),
                            PlayerGeneralStats::new, (stats) -> {
                        PlayerMapStats mapStats = stats.getMapStatsForMap(map);
                        mapStats.setTimesPlayed(mapStats.getTimesPlayed() + 1);
                    });
                    statsManager.queueCacheRequest(CacheInformation.MAP, map.getName(), MapStats::new, (stats) -> {
                        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("America/New_York"));

                        stats.getDecember2021Players().computeIfAbsent(dateTime.getDayOfMonth(), HashSet::new)
                                .add(bukkitPlayer.getUniqueId());
                    });

                    zombiesPlayer.startTasks();
                }
            }

            doRound(0);
        }
    }

    public void doRound(int targetRound) {
        RoundContext context = new RoundContext(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        if(currentRound != null) {
            currentRound.cancelRound();
        }

        currentRound = context;

        Property<Integer> roundIndexProperty = map.getCurrentRoundProperty();

        int lastRoundIndex = targetRound - 1;
        long approximateTicks = (System.currentTimeMillis() - startTimeStamp) / 50; // todo: approximation bad

        for (ZombiesPlayer zombiesPlayer : getPlayerMap().values()) {
            if (zombiesPlayer.isInGame() && !zombiesPlayer.isAlive()) {
                zombiesPlayer.respawn();
            }

            Player player = zombiesPlayer.getPlayer();
            if (player != null) {
                if (map.getRoundTimesShouldSave().contains(lastRoundIndex)) {
                    statsManager.queueCacheRequest(CacheInformation.PLAYER, player.getUniqueId(),
                            PlayerGeneralStats::new, (stats) -> {
                        PlayerMapStats mapStats = stats.getMapStatsForMap(getArena().getMap());
                        mapStats.setRoundsSurvived(mapStats.getRoundsSurvived() + 1);

                        if (mapStats.getBestRound() < lastRoundIndex) {
                            mapStats.setBestRound(lastRoundIndex);
                        }

                        Component bar = MiniMessage.get().parse("<green>=======================");
                        Map<Integer, Long> bestTimes = mapStats.getBestTimes();
                        Long bestTime = bestTimes.get(lastRoundIndex);
                        if (bestTime == null || bestTime < approximateTicks) {
                            bestTimes.put(lastRoundIndex, approximateTicks);
                            Bukkit.getScheduler().runTask(Zombies.getInstance(), () -> {
                                if (player.isOnline()) {
                                    player.sendMessage(bar);
                                    player.sendMessage(MiniMessage.get()
                                            .parse(String.format("<red>You beat round %d in %s!", targetRound,
                                                    TimeUtil.convertTicksToSecondsString(approximateTicks))));
                                    player.sendMessage(MiniMessage.get().parse("<gold>NEW PERSONAL BEST!"));
                                    player.sendMessage(bar);
                                }
                            });
                        }
                        else {
                            Bukkit.getScheduler().runTask(Zombies.getInstance(), () -> {
                                if (player.isOnline()) {
                                    player.sendMessage(bar);
                                    player.sendMessage(MiniMessage.get()
                                            .parse(String.format("<red>You beat round %d in %s!", targetRound,
                                                    TimeUtil.convertTicksToSecondsString(approximateTicks))));
                                    player.sendMessage(bar);
                                }
                            });
                        }
                    });
                } else {
                    statsManager.queueCacheRequest(CacheInformation.PLAYER, player.getUniqueId(),
                            PlayerGeneralStats::new, (stats) -> {
                        PlayerMapStats mapStats = stats.getMapStatsForMap(getArena().getMap());
                        mapStats.setRoundsSurvived(mapStats.getRoundsSurvived() + 1);

                        if (mapStats.getBestRound() < lastRoundIndex) {
                            mapStats.setBestRound(lastRoundIndex);
                        }
                    });
                }
            }
        }

        List<RoundData> rounds = map.getRounds();
        if(targetRound < rounds.size()) {
            RoundData currentRound = rounds.get(targetRound);

            long cumulativeDelay = 0;
            zombiesLeft = 0;
            for (WaveData wave : currentRound.getWaves()) {
                cumulativeDelay += wave.getWaveLength();

                BukkitTask waveSpawnTask = runTaskLater(cumulativeDelay, () -> {
                    List<ActiveMob> newlySpawned = spawner.spawnMobs(wave.getSpawnEntries(), wave.getMethod(),
                            wave.getSlaSquared(), wave.isRandomizeSpawnpoints(), false);
                    context.spawnedMobs().addAll(newlySpawned);

                    for(ActiveMob activeMob : newlySpawned) {
                        MetadataHelper.setFixedMetadata(activeMob.getEntity().getBukkitEntity(), Zombies.getInstance(),
                                MetadataKeys.MOB_WAVE.getKey(), wave);

                        if (map.getSpeedupGrace() < map.getDespawnTicks()) {
                            Entity entity = activeMob.getEntity().getBukkitEntity();
                            if (entity instanceof Mob mob) {
                                AttributeInstance attributeInstance = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                                if (attributeInstance == null) {
                                    mob.registerAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                                    attributeInstance = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                                }
                                if (attributeInstance != null) {
                                    AttributeInstance finalAttributeInstance = attributeInstance;
                                    AttributeModifier[] last = new AttributeModifier[]{null};
                                    double[] value = new double[]{1.0};
                                    // (speedupRate^(period / time)) ^ (time/period)
                                    double rate = Math.pow(map.getMaxSpeedup(), (double) map.getSpeedupIncrementPeriod()
                                            / (map.getDespawnTicks() - map.getSpeedupGrace()));
                                    BukkitTask speedupTask = runTaskTimer(map.getSpeedupGrace(),
                                            map.getSpeedupIncrementPeriod(), new DisposableBukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (!mob.isDead()) {
                                                AttributeModifier finalLast = last[0];
                                                if (finalLast != null) {
                                                    finalAttributeInstance.removeModifier(finalLast);
                                                }
                                                finalAttributeInstance.addModifier(last[0] =
                                                        new AttributeModifier(MOB_SPEEDUP_ATTRIBUTE_NAME,
                                                                (value[0] *= rate) - 1,
                                                                AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                                            } else {
                                                cancel();
                                            }
                                        }
                                    });

                                    context.speedupTasks().add(speedupTask);
                                }
                            }
                        }
                    }

                    BukkitTask removeMobTask = runTaskLater(map.getDespawnTicks(), () -> {
                        for(ActiveMob mob : newlySpawned) {
                            Entity entity = mob.getEntity().getBukkitEntity();

                            if(entity != null) {
                                entity.remove();
                            }
                        }
                    });

                    context.removeTasks().add(removeMobTask);
                });

                context.spawnTasks().add(waveSpawnTask);

                for(SpawnEntryData spawnEntryData : wave.getSpawnEntries()) {
                    zombiesLeft += spawnEntryData.getMobCount();
                }
            }

            roundIndexProperty.setValue(this, targetRound);

            getPlayerMap().forEach((l,r) -> {
                if (r.isInGame()) {
                    Player bukkitPlayer = r.getPlayer();
                    if (bukkitPlayer != null) {
                        bukkitPlayer.showTitle(Title.title(currentRound.getCustomMessage() != null
                                        && !currentRound.getCustomMessage().isEmpty()
                                        ? Component.text(currentRound.getCustomMessage())
                                        : Component.text("ROUND " + (targetRound + 1), NamedTextColor.RED),
                                Component.empty()));
                        bukkitPlayer.playSound(Sound.sound(
                                Key.key("minecraft:entity.wither.spawn"),
                                Sound.Source.MASTER,
                                1.0F,
                                0.5F
                        ));
                    }
                }
            });

            if(getMap().getDisablePowerUpRound().contains(targetRound + 1)) {
                var items =getPowerUps().stream()
                        .filter(x -> x.getState() == PowerUpState.NONE || x.getState() == PowerUpState.DROPPED)
                        .collect(Collectors.toSet());
                items.forEach(PowerUp::deactivate);
            }
        }
        else {
            //game just finished, do win condition
            state = ZombiesArenaState.ENDED;
            doVictory();
        }
    }

    /**
     * Win code here
     */
    private void doVictory() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        long approximateTicks = (endTimeStamp - startTimeStamp) / 50;

        var round = map.getCurrentRoundProperty().getValue(this);
        getPlayerMap().forEach((l,r) -> {
            if (r.isInGame()) {
                Player bukkitPlayer = r.getPlayer();

                if (bukkitPlayer != null) {
                    bukkitPlayer.showTitle(Title.title(Component.text("You Win!", NamedTextColor.GREEN),
                            Component.text("You made it to Round " + (round + 1) + "!", NamedTextColor.GRAY)));
                }
            }
            statsManager.queueCacheRequest(CacheInformation.PLAYER, r.getOfflinePlayer().getUniqueId(),
                    PlayerGeneralStats::new, (playerStats) -> {
                        PlayerMapStats playerMapStats = playerStats.getMapStatsForMap(getArena().getMap());
                        playerMapStats.setWins(playerMapStats.getWins() + 1);

                        if (playerMapStats.getBestTime() == null || approximateTicks < playerMapStats.getBestTime()) {
                            playerMapStats.setBestTime(approximateTicks);
                            statsManager.queueCacheRequest(CacheInformation.MAP, map.getName(), MapStats::new,
                                    (mapStats) -> {
                                        Map<UUID, Long> bestTimes = mapStats.getBestTimes(), bestDecember2021Times = mapStats.getDecember2021Event();
                                        bestTimes.put(r.getOfflinePlayer().getUniqueId(), approximateTicks);
                                        bestDecember2021Times.put(r.getOfflinePlayer().getUniqueId(), approximateTicks);
                                    });
                        }
                    });
        });
        runTaskLater(200L, this::dispose);
    }

    /**
     * Loss code here
     */
    private void doLoss() {
        state = ZombiesArenaState.ENDED;
        endTimeStamp = System.currentTimeMillis();
        var round = map.getCurrentRoundProperty().getValue(this);
        getPlayerMap().forEach((l,r) -> {
            if (r.isInGame()) {
                Player bukkitPlayer = r.getPlayer();
                if (bukkitPlayer != null) {
                    bukkitPlayer.showTitle(Title.title(Component.text("Game Over!", NamedTextColor.GREEN),
                            Component.text("You made it to Round " + (round + 1) + "!", NamedTextColor.GRAY)));
                    bukkitPlayer.sendActionBar(Component.empty());
                }
            }
        });
        gameScoreboard.run();
        runTaskLater(200L, this::dispose);
    }

    /**
     * Attempts to break the given window.
     */
    public void tryBreakWindow(Entity attacker, WindowData targetWindow, int by) {
        targetWindow.getAttackingEntityProperty().setValue(this, attacker);

        int previousIndex = targetWindow.getCurrentIndexProperty().getValue(this);
        int blocksBroken = targetWindow.retractRepairState(this, by);

        for(int i = previousIndex; i > previousIndex - blocksBroken; i--) { //break the blocks
            WorldUtils.getBlockAt(world, targetWindow.getFaceVectors().get(i)).setType(Material.AIR);

            Vector center = targetWindow.getCenter();
            if(i > 0) {
                world.playSound(targetWindow.getBlockBreakSound(), center.getX(), center.getY(), center.getZ());
            }
            else {
                world.playSound(targetWindow.getWindowBreakSound(), center.getX(), center.getY(), center.getZ());
            }
        }
    }

    public boolean runAI() {
        return state == ZombiesArenaState.STARTED;
    }

    /**
     * Gets the shop event for a shop type or creates a new one
     * @param shopType The shop type string representation
     * @return The shop type's event
     */
    public Event<ShopEventArgs> getShopEvent(String shopType) {
        return shopEvents.computeIfAbsent(shopType, (unused) -> new Event<>());
    }

    /**
     * Loads shops; should be called just before the game begins
     */
    private void loadShops() {
        for (ShopData shopData : map.getShops()) {
            if (shopData != null) {
                Shop<?> shop = shopManager.createShop(this, shopData);
                shops.add(shop);
                shopMap.computeIfAbsent(shop.getShopType(), (unused) -> new ArrayList<>()).add(shop);
                getShopEvent(shop.getShopType());
                shop.display();
            }
        }

        for(DoorData doorData : map.getDoors()) {
            if (doorData != null) {
                Shop<DoorData> shop = shopManager.createShop(this, doorData);
                shops.add(shop);
                shopMap.computeIfAbsent(shop.getShopType(), (unused) -> new ArrayList<>()).add(shop);
                shop.display();
            }
        }
        getShopEvent(ShopType.DOOR.name());

        for (Shop<?> shop : shopMap.get(ShopType.TEAM_MACHINE.name())) {
            TeamMachine teamMachine = (TeamMachine) shop;
            getResourceManager().addDisposable(teamMachine);
        }

        Event<ShopEventArgs> chestEvent = getShopEvent(ShopType.LUCKY_CHEST.name());
        List<Shop<?>> shopMapChests = shopMap.get(ShopType.LUCKY_CHEST.name());
        if (chestEvent != null && shopMapChests != null) {
            chestEvent.registerHandler(new EventHandler<>() {

                private final Random random = new Random();
                int rolls = 0;
                {
                    List<Shop<?>> chests = new ArrayList<>(shopMapChests);

                    if (!map.isChestCanStartInSpawnRoom()) {
                        RoomData spawnRoom = map.roomAt(map.getSpawn());
                        chests.removeIf(chest -> {
                            LuckyChest luckyChest = (LuckyChest) chest;
                            return spawnRoom.getBounds().contains(luckyChest.getShopData().getChestLocation());
                        });
                    }

                    LuckyChest luckyChest
                            = (LuckyChest) chests.get(random.nextInt(chests.size()));
                    luckyChest.setActive(true);

                    RoomData room = map.roomAt(luckyChest.getShopData().getChestLocation());
                    luckyChestRoom = room != null ? room.getRoomDisplayName() : null;
                }

                @Override
                public void handleEvent(ShopEventArgs args) {
                    if (++rolls == map.getRollsPerChest()) {
                        LuckyChest luckyChest = (LuckyChest) args.getShop();
                        luckyChest.setActive(false);
                        List<Shop<?>> chests = new ArrayList<>(shopMap.get(luckyChest.getShopType()));
                        chests.remove(luckyChest);

                        LuckyChest nextLuckyChest = ((LuckyChest) chests.get(random.nextInt(chests.size())));
                        nextLuckyChest.setActive(true);
                        RoomData room = map.roomAt(nextLuckyChest.getShopData().getChestLocation());
                        luckyChestRoom = room != null ? room.getRoomDisplayName() : null;

                        rolls = 0;

                        Component message = luckyChestRoom == null
                                ? Component.text("The lucky chest has moved!", NamedTextColor.RED)
                                : Component.text("The lucky chest has moved to " + luckyChestRoom + "!",
                                NamedTextColor.RED);
                        Sound sound = Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER,
                                1.0F, 1.0F);
                        for (ZombiesPlayer player : getPlayerMap().values()) {
                            if (player.isInGame()) {
                                Player bukkitPlayer = player.getPlayer();
                                if (bukkitPlayer != null) {
                                    bukkitPlayer.sendMessage(message);
                                    bukkitPlayer.playSound(sound);
                                }
                            }
                        }
                    }
                }
            });
        }
        Event<ShopEventArgs> piglinShopEvent = getShopEvent(ShopType.PIGLIN_SHOP.name());
        List<Shop<?>> shopMapPiglins = shopMap.get(ShopType.PIGLIN_SHOP.name());
        if (piglinShopEvent != null && shopMapPiglins != null) {
            piglinShopEvent.registerHandler(new EventHandler<>() {

                private final Random random = new Random();
                int rolls = 0;
                {
                    List<Shop<?>> piglins = new ArrayList<>(shopMapPiglins);

                    if (!map.isChestCanStartInSpawnRoom()) {
                        RoomData spawnRoom = map.roomAt(map.getSpawn());
                        piglins.removeIf(piglin -> {
                            PiglinShop piglinShop = (PiglinShop) piglin;
                            return spawnRoom.getBounds().contains(piglinShop.getShopData().getPiglinLocation());
                        });
                    }

                    PiglinShop piglinShop
                            = (PiglinShop) piglins.get(random.nextInt(piglins.size()));
                    piglinShop.setActive(true);

                    RoomData room = map.roomAt(piglinShop.getShopData().getPiglinLocation());
                    piglinRoom = room != null ? room.getRoomDisplayName() : null;
                }

                @Override
                public void handleEvent(ShopEventArgs args) {
                    if (++rolls == map.getRollsPerChest()) {
                        PiglinShop piglinShop = (PiglinShop) args.getShop();
                        piglinShop.setActive(false);
                        List<Shop<?>> piglins = new ArrayList<>(shopMap.get(piglinShop.getShopType()));
                        piglins.remove(piglinShop);

                        PiglinShop nextPiglinShop = ((PiglinShop) piglins.get(random.nextInt(piglins.size())));
                        nextPiglinShop.setActive(true);
                        RoomData room = map.roomAt(nextPiglinShop.getShopData().getPiglinLocation());
                        piglinRoom = room != null ? room.getRoomDisplayName() : null;

                        rolls = 0;

                        Component message = piglinRoom == null
                                ? Component.text("The lucky piglin has moved!", NamedTextColor.RED)
                                : Component.text("The lucky piglin has moved to " + piglinRoom + "!",
                                NamedTextColor.RED);
                        Sound sound = Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER,
                                1.0F, 1.0F);
                        for (ZombiesPlayer player : getPlayerMap().values()) {
                            if (player.isInGame()) {
                                Player bukkitPlayer = player.getPlayer();
                                if (bukkitPlayer != null) {
                                    bukkitPlayer.sendMessage(message);
                                    bukkitPlayer.playSound(sound);
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
