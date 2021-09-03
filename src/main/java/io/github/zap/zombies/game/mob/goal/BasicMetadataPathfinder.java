package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class BasicMetadataPathfinder extends ZombiesPathfinder {
    protected ZombiesPlayer zombiesPlayer;
    private ZombiesArena arena;
    private final double speed;
    private final double targetDeviation;
    protected PathResult result;

    public BasicMetadataPathfinder(Mob mob, AttributeValue[] values, int retargetTicks, double speed,
                                   double targetDeviation) {
        super(mob, values, retargetTicks, Zombies.ARENA_METADATA_NAME, Zombies.WINDOW_METADATA_NAME);
        this.speed = speed;
        this.targetDeviation = targetDeviation;
    }

    protected void retarget() {
        if(arena == null) {
            arena = getMetadata(Zombies.ARENA_METADATA_NAME);
        }

        if(arena != null) {
            ZombiesPlayer closestPlayer = closestPlayer(arena);

            if(closestPlayer != null) {
                PathOperation operation = new PathOperationBuilder()
                        .withAgent(self)
                        .withDestination(Objects.requireNonNull(closestPlayer.getPlayer()), closestPlayer)
                        .withRange(arena.getMapBounds())
                        .build();

                getHandler().queueOperation(operation, self.getWorld());
                PathResult result = getHandler().tryTakeResult();

                if(result != null) {
                    if(result.destination().target() instanceof ZombiesPlayer zombiesPlayer) {
                        Player player = zombiesPlayer.getPlayer();

                        if(player != null) {
                            self.setTarget(player);
                            this.zombiesPlayer = zombiesPlayer;
                            this.result = result;
                        }
                        else {
                            clearTarget();
                        }
                    }
                }
            }
            else {
                clearTarget();
            }
        }
    }

    protected void setPath(@NotNull PathResult result) {
        getNavigator().navigateAlongPath(result.toPathEntity(), speed);
    }

    protected ZombiesPlayer closestPlayer(ZombiesArena arena) {
        double closestDistance = Double.MAX_VALUE;
        ZombiesPlayer closestPlayer = null;

        for(ZombiesPlayer player : arena.getPlayerMap().values()) {
            if(player.isAlive() && player.isInGame()) {
                Player bukkitPlayer = player.getPlayer();
                if(bukkitPlayer != null) {
                    double distance = self.getLocation().distanceSquared(bukkitPlayer.getLocation());
                    if(distance < closestDistance) {
                        closestDistance = distance;
                        closestPlayer = player;
                    }
                }
            }
        }

        return closestPlayer;
    }

    protected void clearTarget() {
        self.setTarget(null);
        zombiesPlayer = null;
    }
}
