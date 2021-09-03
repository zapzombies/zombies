package io.github.zap.zombies.game.equipment2.feature.gun.headshot.historyreader;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnyHeadshotReader implements HeadshotHistoryReader {

    @Override
    public boolean isHeadshot(@NotNull List<Boolean> headshotHistory) {
        for (Boolean headshot : headshotHistory) {
            if (headshot != null && headshot) {
                return true;
            }
        }

        return false;
    }

}
