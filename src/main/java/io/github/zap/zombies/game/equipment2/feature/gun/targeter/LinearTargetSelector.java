package io.github.zap.zombies.game.equipment2.feature.gun.targeter;

import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment2.feature.gun.headshot.Headshotter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("ClassCanBeRecord")
public class LinearTargetSelector implements TargetSelector {

    private final Headshotter headshotter;

    private final Function<Vector, Vector> directionNoiseGenerator;

    private final boolean reuse;

    public LinearTargetSelector(@NotNull Headshotter headshotter,
                                @NotNull Function<Vector, Vector> directionNoiseGenerator, boolean reuse) {
        this.headshotter = headshotter;
        this.directionNoiseGenerator = directionNoiseGenerator;
        this.reuse = reuse;
    }

    @Override
    public @NotNull List<TargetSelection> selectTargets(@NotNull MapData map, @NotNull World world,
                                                        @NotNull Set<Mob> candidates, @NotNull Set<Mob> used,
                                                        @NotNull Vector root, @NotNull Vector previousDirection,
                                                        @NotNull List<Boolean> headshotHistory, int maxRange,
                                                        int limit) {
        List<TargetSelection> selection = new ArrayList<>();

        Vector modifiedPreviousDirection = directionNoiseGenerator.apply(previousDirection);
        double shotRange = getShotRange(map, world, root, previousDirection, maxRange);
        List<Pair<RayTraceResult, Mob>> rayTraces = new ArrayList<>(candidates.size());
        Map<Mob, Double> distances = new HashMap<>();
        for (Mob mob : candidates) {
            if (!reuse && !used.contains(mob)) {
                continue;
            }

            RayTraceResult rayTrace = mob.getBoundingBox().rayTrace(root, modifiedPreviousDirection, shotRange);
            if (rayTrace != null) {
                distances.put(mob, root.distanceSquared(rayTrace.getHitPosition()));
                rayTraces.add(Pair.of(rayTrace, mob));
            }
        }

        rayTraces.sort(Comparator.comparingDouble(o -> distances.get(o.getRight())));

        int bound = Math.min(rayTraces.size(), limit);
        if (bound > 0) {
            Pair<RayTraceResult, Mob> firstRayTrace = rayTraces.get(0);
            boolean firstHeadshot = headshotter.isHeadshot(firstRayTrace.getLeft(), headshotHistory);
            selection.add(new TargetSelection(firstRayTrace.getRight(), modifiedPreviousDirection,
                    firstRayTrace.getLeft().getHitPosition(), true, firstHeadshot));

            for (int i = 0; i < bound; i++) {
                Pair<RayTraceResult, Mob> rayTrace = rayTraces.get(i);
                boolean headshot = headshotter.isHeadshot(rayTrace.getLeft(), headshotHistory);
                selection.add(new TargetSelection(rayTrace.getRight(), modifiedPreviousDirection,
                        rayTrace.getLeft().getHitPosition(), false, headshot));
                used.add(rayTrace.getRight());
            }
        }

        return selection;
    }

    private double getShotRange(@NotNull MapData map, @NotNull World world, @NotNull Vector root,
                                @NotNull Vector direction, int maxRange) {
        Block targetBlock = null;
        Iterator<Block> iterator = new BlockIterator(world, root, direction, 0.0D, maxRange);

        RayTraceResult lastRayTrace = null;
        boolean wallshot = false;
        while (iterator.hasNext()) {
            targetBlock = iterator.next();

            if (!wallshot && !targetBlock.isPassable() && targetBlock.getType() != Material.BARRIER
                    && map.windowAt(targetBlock.getLocation().toVector()) == null) {
                BoundingBox boundingBox = targetBlock.getBoundingBox();
                if (!isFullBlock(boundingBox)) {
                    if (map.isAllowWallshooting()) {
                        wallshot = true;
                        continue;
                    }

                    lastRayTrace = boundingBox.rayTrace(root, direction, maxRange + Math.sqrt(3));
                    if (lastRayTrace == null) {
                        continue;
                    }

                    break;
                }
            }
        }

        if (wallshot) {
            lastRayTrace = targetBlock.getBoundingBox().rayTrace(root, direction, maxRange + Math.sqrt(3));
        }

        return (lastRayTrace != null) ? root.distance(lastRayTrace.getHitPosition()) : maxRange;
    }

    private boolean isFullBlock(@NotNull BoundingBox boundingBox) {
        return boundingBox.getWidthX() == 1.0 && boundingBox.getHeight() == 1.0 && boundingBox.getWidthZ() == 1.0;
    }

}
