package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.FieldTypeDeserializer;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.tmtask.*;
import io.github.zap.zombies.game.shop.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Class for storing and managing shop data using Jackson's data loader
 */
@Getter
public class JacksonShopManager implements ShopManager {

    private final FieldTypeDeserializer<ShopData> shopDataDeserializer
            = new FieldTypeDeserializer<>("type");

    private final ShopCreator shopCreator = new ShopCreator();

    private final FieldTypeDeserializer<TeamMachineTask> teamMachineTaskFieldTypeDeserializer
            = new FieldTypeDeserializer<>("type");

    public JacksonShopManager() {
        ArenaApi arenaApi = ArenaApi.getInstance();

        arenaApi.addDeserializer(ShopData.class, shopDataDeserializer);
        addShop(ShopType.ARMOR_SHOP.name(), ArmorShopData.class, ArmorShop::new);
        addShop(ShopType.DOOR.name(), DoorData.class, Door::new);
        addShop(ShopType.GUN_SHOP.name(), GunShopData.class, GunShop::new);
        addShop(ShopType.LUCKY_CHEST.name(), LuckyChestData.class, LuckyChest::new);
        addShop(ShopType.PIGLIN_SHOP.name(), PiglinShopData.class, PiglinShop::new);
        addShop(ShopType.PERK_MACHINE.name(), PerkMachineData.class, PerkMachine::new);
        addShop(ShopType.POWER_SWITCH.name(), PowerSwitchData.class, PowerSwitch::new);
        addShop(ShopType.TEAM_MACHINE.name(), TeamMachineData.class, TeamMachine::new);
        addShop(ShopType.ULTIMATE_MACHINE.name(), UltimateMachineData.class, UltimateMachine::new);

        arenaApi.addDeserializer(TeamMachineTask.class, teamMachineTaskFieldTypeDeserializer);
        addTeamMachineTask(TeamMachineTaskType.AMMO_SUPPLY.name(), AmmoSupply.class);
        addTeamMachineTask(TeamMachineTaskType.FULL_REVIVE.name(), FullRevive.class);
        addTeamMachineTask(TeamMachineTaskType.DRAGON_WRATH.name(), DragonWrath.class);
    }

    @Override
    public <D extends ShopData> void addShop(@NotNull String shopType, @NotNull Class<D> dataClass,
                                             @NotNull ShopCreator.ShopMapping<D> shopMapping) {
        shopDataDeserializer.getMappings().put(shopType, dataClass);
        shopCreator.getShopMappings().put(shopType, shopMapping);
    }

    @Override
    public void addTeamMachineTask(@NotNull String type, @NotNull Class<? extends TeamMachineTask> clazz) {
        teamMachineTaskFieldTypeDeserializer.getMappings().put(type, clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <D extends ShopData> Shop<D> createShop(@NotNull ZombiesArena arena, @NotNull D shopData) {
        ShopCreator.ShopMapping<D> shopMapping =
                (ShopCreator.ShopMapping<D>) shopCreator.getShopMappings().get(shopData.getType());

        return shopMapping.createShop(arena, shopData);
    }

}
