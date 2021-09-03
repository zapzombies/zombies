package io.github.zap.zombies.game.data.equipment;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.FieldTypeDeserializer;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunData;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.gun.SprayGunData;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunData;
import io.github.zap.zombies.game.data.equipment.melee.AOEMeleeData;
import io.github.zap.zombies.game.data.equipment.melee.BasicMeleeData;
import io.github.zap.zombies.game.data.equipment.perk.*;
import io.github.zap.zombies.game.equipment.*;
import io.github.zap.zombies.game.equipment.gun.*;
import io.github.zap.zombies.game.equipment.melee.AOEMeleeWeapon;
import io.github.zap.zombies.game.equipment.melee.BasicMeleeWeapon;
import io.github.zap.zombies.game.equipment.melee.MeleeObjectGroup;
import io.github.zap.zombies.game.equipment.perk.*;
import io.github.zap.zombies.game.equipment.skill.SkillObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class for storing and managing pieces of equipment using Jackson's data loader
 */
@RequiredArgsConstructor
public class JacksonEquipmentManager implements EquipmentManager {

    private final FieldTypeDeserializer<EquipmentData<?>> equipmentDataDeserializer
            = new FieldTypeDeserializer<>("type");

    private final FieldTypeDeserializer<ParticleDataWrapper<?>> particleDataWrapperDeserializer
            = new FieldTypeDeserializer<>("type") {
        {
            Map<String, Class<? extends ParticleDataWrapper<?>>> mappings = getMappings();
            mappings.put(ParticleDataWrapper.DUST_DATA_NAME, ParticleDataWrapper.DustParticleDataWrapper.class);
            mappings.put(ParticleDataWrapper.BLOCK_DATA_NAME, ParticleDataWrapper.BlockParticleDataWrapper.class);
            mappings.put(ParticleDataWrapper.ITEM_STACK_DATA_NAME,
                    ParticleDataWrapper.ItemStackParticleDataWrapper.class);
        }
    };

    private final EquipmentCreator equipmentCreator = new EquipmentCreator();

    private final EquipmentObjectGroupCreator equipmentObjectGroupCreator = new EquipmentObjectGroupCreator();

    private final Map<String, Map<String, EquipmentData<?>>> equipmentDataMap = new HashMap<>();

    private final DataLoader dataLoader;

    private boolean loaded = false;

    {
        // melee weapons
        addEquipmentType(EquipmentType.BASIC_MELEE.name(), BasicMeleeData.class, BasicMeleeWeapon::new);
        addEquipmentType(EquipmentType.AOE_MELEE.name(), AOEMeleeData.class, AOEMeleeWeapon::new);

        // guns
        addEquipmentType(EquipmentType.LINEAR_GUN.name(), LinearGunData.class, LinearGun::new);
        addEquipmentType(EquipmentType.SPRAY_GUN.name(), SprayGunData.class, SprayGun::new);
        addEquipmentType(EquipmentType.ZAPPER.name(), ZapperGunData.class, ZapperGun::new);
        addEquipmentType(EquipmentType.GUARDIAN.name(), GuardianGunData.class, GuardianGun::new);

        // perks
        addEquipmentType(EquipmentType.EXTRA_HEALTH.name(), ExtraHealthData.class, ExtraHealth::new);
        addEquipmentType(EquipmentType.EXTRA_WEAPON.name(), ExtraWeaponData.class, ExtraWeapon::new);
        addEquipmentType(EquipmentType.FAST_REVIVE.name(), FastReviveData.class, FastRevive::new);
        addEquipmentType(EquipmentType.FLAMING_BULLETS.name(), FlamingBulletsData.class, FlamingBullets::new);
        addEquipmentType(EquipmentType.FROZEN_BULLETS.name(), FrozenBulletsData.class, FrozenBullets::new);
        addEquipmentType(EquipmentType.QUICK_FIRE.name(), QuickFireData.class, QuickFire::new);
        addEquipmentType(EquipmentType.SPEED.name(), SpeedPerkData.class, Speed::new);

        addEquipmentObjectGroupType(EquipmentObjectGroupType.MELEE.name(), MeleeObjectGroup::new);
        addEquipmentObjectGroupType(EquipmentObjectGroupType.GUN.name(), GunObjectGroup::new);
        addEquipmentObjectGroupType(EquipmentObjectGroupType.SKILL.name(), SkillObjectGroup::new);
        addEquipmentObjectGroupType(EquipmentObjectGroupType.PERK.name(), PerkObjectGroup::new);
    }

    public <D extends EquipmentData<L>, L> void addEquipmentType(@NotNull String equipmentType,
                                                                 @NotNull Class<D> dataClass,
                                                                 @NotNull EquipmentCreator
                                                                         .EquipmentMapping<D, L> equipmentMapping) {
        equipmentDataDeserializer.getMappings().put(equipmentType, dataClass);
        equipmentCreator.getEquipmentMappings().put(equipmentType, equipmentMapping);
    }

    public void addEquipmentObjectGroupType(@NotNull String equipmentObjectGroupType,
                                            @NotNull EquipmentObjectGroupCreator
                                                    .EquipmentObjectGroupMapping equipmentObjectGroupMapping) {
        equipmentObjectGroupCreator.getEquipmentObjectGroupMappings().put(equipmentObjectGroupType,
                equipmentObjectGroupMapping);
    }

    @Override
    public @Nullable EquipmentData<?> getEquipmentData(@NotNull String mapName, @NotNull String name) {
        if (!loaded) {
            load();
        }

        Map<String, EquipmentData<?>> dataForMap = equipmentDataMap.get(mapName);
        if(dataForMap != null) {
            return dataForMap.get(name);
        }
        else {
            Zombies.warning("Unable to find equipment data for " + name + " in map " + mapName);
            return null;
        }
    }

    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public @NotNull <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(@NotNull ZombiesArena arena,
                                                                                    @NotNull ZombiesPlayer player,
                                                                                    int slot,
                                                                                    @NotNull String mapName,
                                                                                    @NotNull String equipmentName) {
        return createEquipment(arena, player, slot, (D) getEquipmentData(mapName, equipmentName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(@NotNull ZombiesArena arena,
                                                                                    @NotNull ZombiesPlayer player,
                                                                                    int slot,
                                                                                    @NotNull D equipmentData) {
        EquipmentCreator.EquipmentMapping<D, L> equipmentMapping = (EquipmentCreator.EquipmentMapping<D, L>)
                equipmentCreator.getEquipmentMappings().get(equipmentData.getType());

        return equipmentMapping.createEquipment(arena, player, slot, equipmentData);
    }

    @Override
    public @NotNull EquipmentObjectGroup createEquipmentObjectGroup(@NotNull String equipmentObjectGroupType,
                                                                    @NotNull Player player,
                                                                    @NotNull Set<Integer> slots) {
        return equipmentObjectGroupCreator.getEquipmentObjectGroupMappings().get(equipmentObjectGroupType)
                .createEquipmentObjectGroup(player, slots);
    }

    /**
     * Loads all equipment data
     */
    private void load() {
        if (!loaded) {
            File[] files = dataLoader.getRootDirectory().listFiles();

            //TODO: probably should modify how this is loaded, it really shouldn't be done like this
            ArenaApi.getInstance().addDeserializer(EquipmentData.class, equipmentDataDeserializer);
            ArenaApi.getInstance().addDeserializer(ParticleDataWrapper.class, particleDataWrapperDeserializer);

            if (files != null) {
                for (File file : files) {
                    EquipmentDataMap newEquipmentDataMapping = dataLoader.load(FilenameUtils
                            .getBaseName(file.getName()), EquipmentDataMap.class);

                    if (newEquipmentDataMapping != null) {
                        for (Map.Entry<String, EquipmentData<?>> mapping :
                                newEquipmentDataMapping.getMap().entrySet()) {
                            EquipmentData<?> equipmentData = mapping.getValue();

                            if (equipmentData != null) {
                                equipmentDataMap.computeIfAbsent(mapping.getKey(), (String unused) -> new HashMap<>())
                                        .put(equipmentData.getName(), equipmentData);
                            }
                        }
                    }
                }
            }

            loaded = true;
        }
    }

}
