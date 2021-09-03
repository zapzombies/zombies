package io.github.zap.zombies.game.equipment2.feature.gun.beam;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class ParticleBeam implements Beam {

    private final Consumer<Location> particleSpawner;

    private final int particleCount;

    private final double distanceBetween;

    public ParticleBeam(@NotNull Consumer<Location> particleSpawner, int particleCount, double distanceBetween) {
        this.particleSpawner = particleSpawner;
        this.particleCount = particleCount;
        this.distanceBetween = distanceBetween;
    }

    @Override
    public void send(@NotNull World world, @Nullable Mob target, @NotNull Vector from, @NotNull Vector to,
                     @NotNull Runnable onceHit) {
        Vector root = from.clone();
        Vector direction = to.clone().subtract(root).normalize().multiply(distanceBetween);

        for (int i = 0; i < particleCount; i++) {
            particleSpawner.accept(root.add(direction).toLocation(world));
        }

        onceHit.run();
    }

}
