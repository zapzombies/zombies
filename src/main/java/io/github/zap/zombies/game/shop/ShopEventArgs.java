package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Value;

/**
 * Event args for when a shop item is purchased by a player
 */
@Value
public class ShopEventArgs {

    Shop<?> shop;

    ZombiesPlayer zombiesPlayer;

}
