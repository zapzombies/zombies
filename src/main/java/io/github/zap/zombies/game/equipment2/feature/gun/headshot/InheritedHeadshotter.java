package io.github.zap.zombies.game.equipment2.feature.gun.headshot;

import io.github.zap.zombies.game.equipment2.feature.gun.headshot.historyreader.HeadshotHistoryReader;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class InheritedHeadshotter implements Headshotter {

    private final boolean inverted;

    private final HeadshotHistoryReader reader;

    private final Headshotter fallback;

    public InheritedHeadshotter(boolean inverted, @NotNull HeadshotHistoryReader reader,
                                @NotNull Headshotter fallback) {
        this.inverted = inverted;
        this.reader = reader;
        this.fallback = fallback;
    }

    @Override
    public boolean isHeadshot(@NotNull RayTraceResult rayTraceResult, @NotNull List<Boolean> headshotHistory) {
        if (headshotHistory.isEmpty()) {
            return fallback.isHeadshot(rayTraceResult, headshotHistory);
        }

        return inverted != reader.isHeadshot(headshotHistory); // see BasicHeadshotter
    }
}
