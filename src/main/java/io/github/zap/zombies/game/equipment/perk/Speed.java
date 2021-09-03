package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.RepeatingEvent;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.SpeedPerkData;
import io.github.zap.zombies.game.data.equipment.perk.SpeedPerkLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lets players run faster
 */
public class Speed extends RepeatingEventPerk<SpeedPerkData, SpeedPerkLevel> {

    public Speed(@NotNull ZombiesArena arena, @NotNull ZombiesPlayer player, int slot,
                 @NotNull SpeedPerkData perkData) {
        super(arena, player, slot, perkData,
                new RepeatingEvent(Zombies.getInstance(), 0, perkData.getSpeedReapplyInterval()));
    }

    @Override
    public void execute(@Nullable EmptyEventArgs args) {
        Player player = getZombiesPlayer().getPlayer();
        if (player != null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, getCurrentLevel().getDuration(),
                    getCurrentLevel().getAmplifier(), true, false, false));
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        Player player = getZombiesPlayer().getPlayer();
        if (player != null) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }
}
