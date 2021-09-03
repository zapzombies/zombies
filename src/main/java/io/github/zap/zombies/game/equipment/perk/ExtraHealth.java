package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.ExtraHealthData;
import io.github.zap.zombies.game.data.equipment.perk.ExtraHealthLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExtraHealth extends MarkerPerk<ExtraHealthData, ExtraHealthLevel> {

    private AttributeModifier currentMod;

    public ExtraHealth(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot, @NotNull ExtraHealthData perkData) {
        super(arena, player, slot, perkData);
    }

    @Override
    public void activate() {
        Player player = getZombiesPlayer().getPlayer();
        if (player != null) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (attribute != null) {
                if (currentMod != null) {
                    attribute.removeModifier(currentMod);
                }

                currentMod = new AttributeModifier("extra thicc", getCurrentLevel().getAdditionalHealth(),
                        AttributeModifier.Operation.ADD_NUMBER);
                attribute.addModifier(currentMod);
            } else {
                Zombies.warning("Could not get GenericAttributes.MAX_HEALTH for Player.");
            }
        }
    }

    @Override
    public void deactivate() {
        Player player = getZombiesPlayer().getPlayer();
        if (player != null) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (attribute != null && currentMod != null) {
                attribute.removeModifier(currentMod);
            }
        }
    }

}
