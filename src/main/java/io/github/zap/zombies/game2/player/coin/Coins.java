package io.github.zap.zombies.game2.player.coin;

import io.github.zap.zombies.game2.player.MessagePipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class Coins {

    private final int maxCoins;

    private final int minCoins;

    private int coins;

    public Coins(int startingCoins, int maxCoins, int minCoins) {
        this.maxCoins = maxCoins;
        this.minCoins = minCoins;
        this.coins = startingCoins;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(@NotNull MessagePipe messagePipe, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative!");
        }

        if (Integer.MAX_VALUE - coins - amount > 0 && maxCoins - coins - amount > 0) {
            coins += amount;
        }
        else {
            coins = maxCoins;
        }
    }

    public void removeCoins(@NotNull MessagePipe messagePipe, int change) {
        if (change < 0) {
            throw new IllegalArgumentException("change cannot be negative!");
        }

        if (Integer.MIN_VALUE - coins + change > 0 && minCoins - coins + change > 0) {
            coins -= change;
        }
        else {
            coins = minCoins;
        }

        messagePipe.sendMessage(Component.text("-" + change + " Gold", NamedTextColor.GOLD));
    }

}
