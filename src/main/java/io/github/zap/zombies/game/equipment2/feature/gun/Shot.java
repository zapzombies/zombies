package io.github.zap.zombies.game.equipment2.feature.gun;

import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment2.feature.gun.beam.Beam;
import io.github.zap.zombies.game.equipment2.feature.gun.targeter.TargetSelection;
import io.github.zap.zombies.game.equipment2.feature.gun.targeter.TargetSelector;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public class Shot {

    private final Beam beam;

    private final TargetSelector targetSelector;

    private final List<HitHandler> hitHandlers;

    private final int range;

    public Shot(@NotNull Beam beam, @NotNull TargetSelector targetSelector, @NotNull List<HitHandler> hitHandlers,
                int range) {
        this.beam = beam;
        this.targetSelector = targetSelector;
        this.hitHandlers = hitHandlers;
        this.range = range;
    }

    public void shoot(@NotNull MapData map, @NotNull World world, @NotNull Damager damager,
                      @NotNull Set<Mob> candidates, @NotNull Set<Mob> used, @NotNull Vector root,
                      @NotNull Vector previousDirection, @NotNull List<Boolean> headshotHistory) {
        List<TargetSelection> selections = targetSelector.selectTargets(map, world, candidates, used, root,
                previousDirection, headshotHistory, range, hitHandlers.size());

        if (selections.isEmpty() && headshotHistory.isEmpty()) {
            beam.send(world, null, root, root.clone().add(previousDirection.clone().multiply(range)), () -> {});
        } else for (int i = 0; i < selections.size(); i++) {
            TargetSelection selection = selections.get(i);
            if (selection.sendBeam()) {
                int finalI = i;
                beam.send(world, null, root, selection.location(),
                        () -> hitHandlers.get(finalI).hit(map, world, damager, selection, candidates, used,
                                headshotHistory));
            } else hitHandlers.get(i).hit(map, world, damager, selection, candidates, used, headshotHistory);
        }
    }

}
