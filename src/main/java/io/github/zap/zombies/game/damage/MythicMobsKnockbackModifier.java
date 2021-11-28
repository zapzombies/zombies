package io.github.zap.zombies.game.damage;

import io.lumine.xikage.mythicmobs.mobs.MobManager;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class MythicMobsKnockbackModifier implements KnockbackModifier {

    private final static String KNOCKBACK_FACTOR_FIELD = "KnockbackFactor";

    private final MobManager mobManager;

    public MythicMobsKnockbackModifier(@NotNull MobManager mobManager) {
        this.mobManager = mobManager;
    }

    @Override
    public double modify(@NotNull Mob target, double currentKnockback) {
        return mobManager
                .getActiveMob(target.getUniqueId())
                .map(mob -> mob.getType().getConfig().getDouble(KNOCKBACK_FACTOR_FIELD, 1.0D))
                .orElse(currentKnockback);

    }

}
