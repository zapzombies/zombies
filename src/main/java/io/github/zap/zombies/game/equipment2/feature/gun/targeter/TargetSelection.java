package io.github.zap.zombies.game.equipment2.feature.gun.targeter;

import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public record TargetSelection(@NotNull Mob mob, @NotNull Vector direction, @NotNull Vector location, boolean sendBeam,
                              boolean headshot) {

}
