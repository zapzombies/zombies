package io.github.zap.zombies.game.data.equipment.perk;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Level of the extra weapon perk
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class ExtraWeaponLevel extends PerkLevel {

    /**
     * Maps equipment object group types to new slots
     */
    private Map<String, Set<Integer>> newSlots = new HashMap<>();

}
