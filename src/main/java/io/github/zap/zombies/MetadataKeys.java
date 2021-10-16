package io.github.zap.zombies;

import io.github.zap.arenaapi.Keyed;
import org.jetbrains.annotations.NotNull;

public enum MetadataKeys implements Keyed {
    MOB_SPAWN("mob.spawn"),
    MOB_WAVE("mob.wave"),
    SKILL_SHOOTSKULL("skill.shootskull"),
    SKILL_SPAWNMOBS("skill.spawnmobs"),
    SKILL_ARROWBARRAGE("skill.arrowbarrage");

    private final String key;

    MetadataKeys(@NotNull String key) {
        this.key = key;
    }

    @Override
    public @NotNull String getKey() {
        return key;
    }
}
