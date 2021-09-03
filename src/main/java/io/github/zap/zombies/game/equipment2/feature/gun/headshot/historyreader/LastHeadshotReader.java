package io.github.zap.zombies.game.equipment2.feature.gun.headshot.historyreader;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LastHeadshotReader implements HeadshotHistoryReader {

    @Override
    public boolean isHeadshot(@NotNull List<Boolean> headshotHistory) {
        if (headshotHistory.isEmpty()) {
            throw new IllegalArgumentException("Tried to read the last headshot of an empty history!");
        }

        Boolean headshot = headshotHistory.get(headshotHistory.size() - 1);
        if (headshot == null) {
            throw new IllegalArgumentException("Tried to read the last headshot of a history with a " +
                    "null last headshot!");
        }

        return headshot;
    }

}
