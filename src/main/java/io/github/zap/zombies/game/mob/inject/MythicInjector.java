package io.github.zap.zombies.game.mob.inject;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfinderAdapter;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * General interface for injecting custom goals and skills into MythicMobs. Exists to support multiple MythicMobs
 * versions if breaking changes are made.
 */
public interface MythicInjector {
    void injectGoals(@NotNull Iterable<Class<? extends PathfinderAdapter>> goalClasses);

    void injectSkills(@NotNull Iterable<Class<? extends SkillMechanic>> skillClasses);

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    static @Nullable MythicInjector forInstance(@NotNull Logger logger, @NotNull MythicMobs mythicMobs) {
        return switch (mythicMobs.getVersion()) {
            case "4.12.0" -> new MythicInjector_v4_12_R0(logger, mythicMobs);
            default -> null;
        };
    }
}
