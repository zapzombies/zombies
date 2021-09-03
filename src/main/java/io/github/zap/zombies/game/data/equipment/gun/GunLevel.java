package io.github.zap.zombies.game.data.equipment.gun;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * A level of a gun that contains all of its numerical statistics
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class GunLevel  {

    int range;

    float damage;

    double knockbackFactor;

    int ammo;

    int clipAmmo;

    int fireRate;

    int reloadRate;

    int goldPerShot;

    int goldPerHeadshot;

    int shotsPerClick = 1;

    protected GunLevel() {

    }

}
