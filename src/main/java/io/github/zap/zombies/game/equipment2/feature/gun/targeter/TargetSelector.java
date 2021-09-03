package io.github.zap.zombies.game.equipment2.feature.gun.targeter;

import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface TargetSelector {

    @NotNull List<TargetSelection> selectTargets(@NotNull MapData map, @NotNull World world,
                                                 @NotNull Set<Mob> candidates, @NotNull Set<Mob> used,
                                                 @NotNull Vector root, @NotNull Vector previousDirection,
                                                 @NotNull List<Boolean> headshotHistory, int maxRange, int limit);

}
