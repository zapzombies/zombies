package io.github.zap.zombies.game.data.map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpawnEntryData {

    String mobName = "default";

    int mobCount = 0;

    private SpawnEntryData() {}
}
