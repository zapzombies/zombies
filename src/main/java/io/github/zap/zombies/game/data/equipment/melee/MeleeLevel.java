package io.github.zap.zombies.game.data.equipment.melee;

import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;

/**
 * Level of a melee weapon
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class MeleeLevel {

    @Getter
    public static class EnchantmentLevel {

        private Enchantment enchantment;

        private int level;

    }

    private List<EnchantmentLevel> enchantments = new ArrayList<>();

    private long delayTicks = 0L;

    private boolean usesShields = true;

    private boolean shouldSweep = false;

    private int goldPerHit = 10;

    private int goldPerCritical = 15;

    private double damage = 7.0;

    private double knockbackFactor = 1.0;

    protected MeleeLevel() {

    }

}
