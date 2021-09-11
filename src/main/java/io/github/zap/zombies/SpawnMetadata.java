package io.github.zap.zombies;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WindowData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SpawnMetadata(@NotNull ZombiesArena arena, @Nullable WindowData windowData) { }
