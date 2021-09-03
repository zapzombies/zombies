package io.github.zap.zombies.game.data.map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundData {
    /**
     * Message displayed when the round starts. overrides the normal "Round #" message. Can be null
     */
    String customMessage = null;

    List<WaveData> waves = new ArrayList<>();

    public RoundData() {}
}
