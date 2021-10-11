package io.github.zap.zombies.game.mob.inject;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfinderAdapter;
import io.lumine.xikage.mythicmobs.skills.SkillManager;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import io.lumine.xikage.mythicmobs.volatilecode.handlers.VolatileAIHandler;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class MythicInjector_v4_12_R0 implements MythicInjector {
    private final Logger logger;
    private final MythicMobs mythicMobs;

    MythicInjector_v4_12_R0(@NotNull Logger logger, @NotNull MythicMobs mythicMobs) {
        this.logger = logger;
        this.mythicMobs = mythicMobs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void injectGoals(@NotNull Iterable<Class<? extends PathfinderAdapter>> goalClasses) {
        VolatileAIHandler handler = mythicMobs.getVolatileCodeHandler().getAIHandler();

        try {
            Field aiGoalsField = handler.getClass().getDeclaredField("AI_GOALS");
            aiGoalsField.setAccessible(true);

            Map<String, Class<? extends PathfinderAdapter>> aiGoals =
                    (Map<String, Class<? extends PathfinderAdapter>>) aiGoalsField.get(handler);

            for (Class<? extends PathfinderAdapter> customGoal : goalClasses) {
                MythicAIGoal mythicAnnotation = customGoal.getAnnotation(MythicAIGoal.class);

                if (mythicAnnotation != null) {
                    aiGoals.put(mythicAnnotation.name().toUpperCase(), customGoal);

                    for (String alias : mythicAnnotation.aliases()) {
                        aiGoals.put(alias.toUpperCase(), customGoal);
                    }
                } else {
                    logger.warning("Encountered a custom goal class not annotated with @MythicAIGoal: " +
                            customGoal.getTypeName());
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            logger.log(Level.WARNING, "Failed to reflect AI goal map", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void injectSkills(@NotNull Iterable<Class<? extends SkillMechanic>> skillClasses) {
        try {
            Field mechanicsField = SkillManager.class.getDeclaredField("MECHANICS");
            mechanicsField.setAccessible(true);

            Map<String, Class<? extends SkillMechanic>> mechanics =
                    (Map<String, Class<? extends SkillMechanic>>) mechanicsField.get(null);

            for (Class<? extends SkillMechanic> customMechanic : skillClasses) {
                MythicMechanic mythicAnnotation = customMechanic.getAnnotation(MythicMechanic.class);

                if (mythicAnnotation != null) {
                    mechanics.put(mythicAnnotation.name().toUpperCase(), customMechanic);

                    for (String alias : mythicAnnotation.aliases()) {
                        mechanics.put(alias.toUpperCase(), customMechanic);
                    }
                } else {
                    logger.warning( "Encountered a custom mechanic class not annotated with @MythicMechanic: " +
                            customMechanic.getTypeName());
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            logger.log(Level.WARNING, "Failed to reflect mechanics map", e);
        }
    }
}
