package io.github.zap.zombies.game.equipment2.feature.gun.headshot.historyreader;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface HeadshotHistoryReader {

    boolean isHeadshot(@NotNull List<Boolean> headshotHistory);

}
