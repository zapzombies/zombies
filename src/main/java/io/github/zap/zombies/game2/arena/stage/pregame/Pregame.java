package io.github.zap.zombies.game2.arena.stage.pregame;

import io.github.zap.zombies.game2.arena.stage.Stage;
import io.github.zap.zombies.game2.arena.stage.StageCompletion;
import io.github.zap.zombies.game2.arena.stage.StageRequirement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class Pregame implements Stage {

    private final StageRequirement requirement;

    public Pregame(@NotNull StageRequirement requirement) {
        this.requirement = Objects.requireNonNull(requirement, "requirement cannot be null!");
    }

    @Override
    public void begin() {

    }

    @Override
    public @NotNull StageCompletion tick() {
        if (requirement.isMet()) {
            return StageCompletion.CONTINUE;
        }

        return StageCompletion.NEXT;
    }

    @Override
    public void cancel() {

    }

}
