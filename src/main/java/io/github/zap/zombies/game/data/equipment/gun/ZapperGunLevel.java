package io.github.zap.zombies.game.data.equipment.gun;

import lombok.Getter;

/**
 * Level of a zapper gun
 */
@Getter
public class ZapperGunLevel extends LinearGunLevel {
    private int maxChainedEntities;

    private double maxChainDistance;

    private double aoeHitDamageFactor;

}
