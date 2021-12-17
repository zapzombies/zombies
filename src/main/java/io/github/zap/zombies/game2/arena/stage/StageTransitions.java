package io.github.zap.zombies.game2.arena.stage;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class StageTransitions {

    private final static int TRANSITION_NOT_STARTED = -1;

    private final List<Stage> stages;

    private Stage stage = null;

    private int index = TRANSITION_NOT_STARTED;

    public StageTransitions(@NotNull List<Stage> stages) {
        this.stages = Objects.requireNonNull(stages, "stages cannot be null!");
    }

    public void tick() {
        if (index >= stages.size()) {
            throw new IllegalStateException("The stage transitions are complete!");
        }

        if (index == TRANSITION_NOT_STARTED) {
            index = 0;

            stage = stages.get(index);
            stage.begin();
        }

        // stages can complete in 0 ticks, so we must tick as many as possible
        boolean complete = false;
        while (!complete) {
            StageCompletion completion = stage.tick();

            switch (completion) {
                case BACK -> {
                    stage.cancel();

                    if (index == 0) {
                        index = TRANSITION_NOT_STARTED;
                        stage = null;

                        complete = true;
                    }
                    else {
                        stage = stages.get(--index);
                        stage.begin();
                    }
                }
                case CONTINUE -> {
                    complete = true;
                }
                case NEXT -> {
                    stage.cancel();
                    if (++index < stages.size()) {
                        stage = stages.get(index);
                        stage.begin();
                    }
                }
            }
        }
    }

}
