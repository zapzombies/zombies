package io.github.zap.zombies.game.equipment2.feature.gun.beam;

import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.NotImplementedException;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuardianBeam implements Beam {

    @Override
    public void send(@NotNull World world, @Nullable Mob target, @NotNull Vector from, @NotNull Vector to,
                     @NotNull Runnable onceHit) {
        throw new NotImplementedException();
    }

}
