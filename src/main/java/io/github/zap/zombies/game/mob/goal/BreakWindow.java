package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.operation.PathOperationBuilder;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.commons.vectors.Vectors;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WindowData;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

public class BreakWindow extends BasicMetadataPathfinder {
    private ZombiesArena arena;
    private WindowData window;
    private Vector destination;
    private boolean completed;

    private int counter;
    private int navCounter;

    private final int breakTicks;
    private final int breakCount;
    private final double breakReachSquared;

    public BreakWindow(Mob entity, AttributeValue[] values, int retargetTicks, double speed, double targetDeviation,
                       int breakTicks, int breakCount, double breakReachSquared) {
        super(entity, values, retargetTicks, speed, targetDeviation);
        this.breakTicks = breakTicks;
        this.breakCount = breakCount;
        this.breakReachSquared = breakReachSquared;
        navCounter = getArenaNmsBridge().entityBridge().getRandomFor(self).nextInt(retargetTicks / 2);
    }

    public boolean isValid() {
        return self instanceof Creature;
    }

    @Override
    public boolean canStart() {
        if(!completed) {
            if(arena == null && destination == null && window == null) {
                arena = getMetadata(Zombies.ARENA_METADATA_NAME);
                window = getMetadata(Zombies.WINDOW_METADATA_NAME);

                if(window != null) {
                    destination = window.getTarget();

                    if(destination == null) {
                        Zombies.warning("Entity " + self.getUniqueId() + " spawned in a window that does not" +
                                " supply a target destination!");
                        completed = true;
                        return false;
                    }
                }
                else {
                    completed = true;
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean shouldEnd() {
        return completed || !arena.runAI();
    }

    @Override
    public void start() { }

    @Override
    public void end() { }

    @Override
    public void tick() {
        if(++counter == breakTicks) {
            Vector center = window.getCenter();
            if (getArenaNmsBridge().entityBridge().distanceTo(self, center.getX(), center.getY(), center.getZ())
                    < breakReachSquared) {
                arena.tryBreakWindow(self, window, breakCount);
            }

            counter = 0;
        }

        Location location = self.getLocation();
        if (!(window.getFaceBounds().contains(location.getX(), location.getY(), location.getZ()) ||
                window.getInteriorBounds().contains(location.getX(), location.getY(), location.getZ()))) {
            Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);
            if(attackingEntity != null && self.getUniqueId() == attackingEntity.getUniqueId()) {
                window.getAttackingEntityProperty().setValue(arena, null);
            }

            completed = true;
        }
        else {
            if(++navCounter == retargetTicks) {
                PathOperation operation = new PathOperationBuilder()
                        .withAgent(self)
                        .withDestination(Vectors.asIntFloor(Vectors.of(destination)))
                        .withRange(2)
                        .build();

                getHandler().giveOperation(operation, arena.getWorld());
                navCounter = getArenaNmsBridge().entityBridge().getRandomFor(self).nextInt(retargetTicks / 2);
            }

            PathResult result = getHandler().tryTakeResult();
            if(result != null) {
                getNavigator().navigateAlongPath(result.toPathEntity(), 1);
            }
        }
    }
}
