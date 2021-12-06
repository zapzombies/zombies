package io.github.zap.zombies.leaderboard.times;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param colonColor The color of colons in the time {@link Component}
     * @param digitColor The color of digits in the time {@link Component}
     * @return A new {@link TimesFormatter}
     */
    static @NotNull TimesFormatter defaultFormatter(@Nullable NamedTextColor colonColor,
                                                    @Nullable NamedTextColor digitColor) {
        return (ticks) -> {
            long seconds = ticks / 20;
            long minutes = seconds / 60;
            seconds %= 60;
            long hours = minutes / 60;
            minutes %= 60;

            Component colon = Component.text(":");
            if (colonColor != null) {
                colon = colon.color(digitColor);
            }

            TextComponent.Builder builder = Component.text();
            if (hours != 0) {
                builder.append(Component.text(hours, digitColor), colon);
            }
            if (minutes != 0) {
                builder.append(Component.text(minutes, digitColor));
            }
            else {
                builder.append(Component.text("0", digitColor));
            }
            builder.append(colon);

            if (seconds < 10) {
                builder.append(Component.text("0", digitColor));
            }
            builder.append(Component.text(seconds, digitColor));

            return builder.build();
        };
    }

}
