package io.github.zap.zombies.game2.player.state;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
public interface Condition {

    void apply(@NotNull Player player);

    static @NotNull Condition defaultConditions() {
        return (player) -> {
            player.setFoodLevel(20);
            player.setSaturation(20.0F);
            player.setHealth(20.0D);
            player.setInvulnerable(true);
            player.setInvisible(false);
            player.setWalkSpeed(0.2F);
            player.setFallDistance(0.0F);
            player.setAllowFlight(false);
            player.setCollidable(true);
            player.setFallDistance(0.0F);
            player.setFlySpeed(0.1F);
            player.setGameMode(GameMode.ADVENTURE);
            player.setArrowsInBody(0);
            player.setLevel(0);
            player.setExp(0.0F);
            player.setFireTicks(0);

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) {

                for (AttributeModifier modifier : attribute.getModifiers()) {
                    attribute.removeModifier(modifier);
                }
            }
        };
    }

}
