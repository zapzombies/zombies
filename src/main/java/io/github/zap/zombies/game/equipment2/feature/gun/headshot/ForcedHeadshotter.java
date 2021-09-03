package io.github.zap.zombies.game.equipment2.feature.gun.headshot;

import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class ForcedHeadshotter implements Headshotter {

    private final boolean headshot;

    public ForcedHeadshotter(boolean headshot) {
        this.headshot = headshot;
    }

    @Override
    public boolean isHeadshot(@NotNull RayTraceResult rayTraceResult, @NotNull List<Boolean> headshotHistory) {
        return headshot;
    }

}
