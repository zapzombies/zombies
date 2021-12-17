package io.github.zap.zombies.game2.player.armor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerArmor {

    private final ItemStack[] armor = new ItemStack[4];

    public void apply(@NotNull Player player) {
        player.getInventory().setArmorContents(armor);
    }

    public void getArmor(@NotNull ItemStack[] armor) {
        copyArmor(this.armor, armor);
    }

    public void setArmor(@NotNull ItemStack[] armor) {
        copyArmor(armor, this.armor);
    }

    private void copyArmor(@NotNull ItemStack[] source, @NotNull ItemStack[] destination) {
        System.arraycopy(source, 0, destination, 0, destination.length);
    }

}
