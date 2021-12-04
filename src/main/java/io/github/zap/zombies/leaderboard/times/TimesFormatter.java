package io.github.zap.zombies.leaderboard.times;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Formats ticks into a {@link Component}
 */
@FunctionalInterface
public interface TimesFormatter {

    /**
     * Formats a number of ticks
     * @param ticks The number of ticks to format
     * @return A {@link Component} representation of the number of ticks
     */
    @NotNull Component format(long ticks);

    /**
     * Default formatter with the form h:mm:ss
     * @return A new {@link TimesFormatter}
     */
    static @NotNull TimesFormatter defaultFormatter() {
        return (ticks) -> {
            long seconds = ticks / 20;
            long minutes = seconds / 60;
            seconds %= 60;
            long hours = minutes / 60;
            minutes %= 60;

            TextComponent.Builder builder = Component.text();
            if (hours != 0) {
                builder.append(Component.text(hours), Component.text(":"));
            }
            if (minutes != 0) {
                builder.append(Component.text(minutes));
            }
            else {
                builder.append(Component.text("0"));
            }
            builder.append(Component.text(":"));

            if (seconds < 10) {
                builder.append(Component.text("0"));
            }
            builder.append(Component.text(seconds));

            builder.color(NamedTextColor.YELLOW);

            return builder.build();
        };
    }

}
