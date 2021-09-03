package io.github.zap.zombies.game.equipment2.feature.gun.direction;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SuppressWarnings("ClassCanBeRecord")
public class RandomAngleDirectionNoiseGenerator implements DirectionNoiseGenerator {

    private final Random random;

    private final double angle;

    public RandomAngleDirectionNoiseGenerator(@NotNull Random random, double angle) {
        this.random = random;
        this.angle = Math.toRadians(angle);
    }

    @Override
    public void modify(@NotNull Vector vector) {
        double yaw = Math.atan2(vector.getZ(), vector.getX());
        if (yaw < 0) yaw += (2 * Math.PI);
        double noYMagnitude = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() + vector.getZ());
        double pitch = Math.atan2(vector.getY(), noYMagnitude);

        yaw += angle * (2 * random.nextDouble() - 1);
        pitch += angle * (2 * random.nextDouble() - 1);

        // https://stackoverflow.com/questions/30011741/3d-vector-defined-by-2-angles
        vector.setX(Math.cos(yaw) * Math.cos(pitch));
        vector.setY(Math.sin(pitch));
        vector.setZ(Math.sin(yaw) * Math.cos(pitch));
    }

}
