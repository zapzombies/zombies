package io.github.zap.zombies.game2.arena.stage;

import org.jetbrains.annotations.NotNull;

public interface Stage {

    void begin();

    @NotNull StageCompletion tick();

    void cancel();

}
