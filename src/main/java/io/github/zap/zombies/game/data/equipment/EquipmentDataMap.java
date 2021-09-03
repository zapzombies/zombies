package io.github.zap.zombies.game.data.equipment;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps zombies map names to equipment datas
 */
public class EquipmentDataMap {

    @Getter
    private final Map<String, EquipmentData<?>> map = new HashMap<>();

    private EquipmentDataMap() {

    }

}
