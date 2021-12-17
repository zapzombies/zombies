package io.github.zap.zombies.game2.arena.cleanup;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class HubWarp implements PlayerCleanup {

    private final Logger logger;

    private final Location hubLocation;

    private final Consumer<Player> fallback;

    public HubWarp(@NotNull Logger logger, @NotNull Location hubLocation, @NotNull Consumer<Player> fallback) {
        this.logger = Objects.requireNonNull(logger, "logger cannot be null!");
        this.hubLocation = Objects.requireNonNull(hubLocation, "hubLocation cannot be null!");
        this.fallback = Objects.requireNonNull(fallback, "fallback cannot be null!");
    }

    public HubWarp(@NotNull Logger logger, @NotNull Location hubLocation) {
        this(logger, hubLocation, (player) -> {
            player.kick(Component.text("Couldn't figure out where to send you!", NamedTextColor.RED));
        });
    }

    @Override
    public void apply(@NotNull Player player) {
        if (!hubLocation.isWorldLoaded()) {
            logger.warning("Failed to warp " + player.getName() + " (UUID: " + player.getUniqueId() + ") to the " +
                    "specified hub at " + hubLocation + ", executing fallback...");
            fallback.accept(player);

            return;
        }

        player.teleportAsync(hubLocation);
    }

}
