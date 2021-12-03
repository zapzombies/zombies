package io.github.zap.zombies.leaderboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface LeaderboardLineCreator {

    @NotNull Component create(int index, @NotNull LeaderboardEntry entry);

    /**
     * Default line creator of the format:
     * #place - player - value
     * @return A new default LeaderboardLineCreator
     */
    static @NotNull LeaderboardLineCreator defaultCreator() {
        return (index, entry) -> TextComponent.ofChildren(
                Component.text().append(
                        Component.text("#"),
                        Component.text(index + 1),
                        Component.text(".")
                ).color(NamedTextColor.YELLOW).build(),
                Component.space(),
                Component.text("-", NamedTextColor.WHITE),
                Component.space(),
                entry.player(),
                Component.space(),
                Component.text("-", NamedTextColor.WHITE),
                Component.space(),
                entry.value()
        );
    }

}
