package io.github.zap.zombies.game2.player;

import io.github.zap.zombies.game2.player.armor.PlayerArmor;
import io.github.zap.zombies.game2.player.coin.Coins;
import io.github.zap.zombies.game2.player.task.PlayerTask;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class ZombiesPlayer extends BasePlayer {

    private final Coins coins;

    private final PlayerArmor armor;

    private final Collection<PlayerTask> tasks;

    private int kills = 0;

    public ZombiesPlayer(@NotNull Server server, @NotNull UUID playerUUID, @NotNull Coins coins,
                         @NotNull PlayerArmor armor, @NotNull Collection<PlayerTask> tasks) {
        super(server, playerUUID);

        this.coins = Objects.requireNonNull(coins, "coins cannot be null!");
        this.armor = Objects.requireNonNull(armor, "armor cannot be null!");
        this.tasks = Objects.requireNonNull(tasks, "tasks cannot be null!");
    }

    public @NotNull Coins getCoins() {
        return coins;
    }

    public @NotNull PlayerArmor getArmor() {
        return armor;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void tick() {
        for (PlayerTask task : tasks) {
            task.tick(this);
        }
    }

}
