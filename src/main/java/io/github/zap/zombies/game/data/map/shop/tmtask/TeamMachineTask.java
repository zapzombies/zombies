package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.arenaapi.Property;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task usable by a team machine
 */
@Getter
public abstract class TeamMachineTask {

    private final String type;

    private String displayName;

    private List<String> lore;

    private Material displayMaterial;

    private int initialCost;

    private final transient Property<Integer> timesUsed = new Property<>(0);

    public TeamMachineTask(String type) {
        this.type = type;
    }

    /**
     * Executes the team machine task
     * @param teamMachine The team machine that called the task
     * @param zombiesArena The arena the team machine is in
     * @param zombiesPlayer The executing player
     * @return Whether the execution was successful
     */
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        Player player = zombiesPlayer.getPlayer();

        if (player != null) {
            int cost = getCostForTeamMachine(teamMachine);
            if (zombiesPlayer.getCoins() < cost) {
                player.sendMessage(Component.text("You cannot afford this item!", NamedTextColor.RED));

                player.playSound(Sound.sound(
                        Key.key("minecraft:entity.enderman.teleport"),
                        Sound.Source.MASTER,
                        1.0F,
                        0.5F
                ));
            } else {
                timesUsed.setValue(teamMachine, timesUsed.getValue(teamMachine) + 1);
                zombiesPlayer.subtractCoins(cost);

                return true;
            }
        }

        return false;
    }

    /**
     * Gets the current cost of the team machine task to purchase for a single team machine
     * @return The current cost for the team machine
     */
    protected abstract int getCostForTeamMachine(TeamMachine teamMachine);

    /**
     * Gets the item stack representation of the team machine task to be used in a team machine
     * @param teamMachine The team machine to get the task's cost for
     * @return The item stack representation of the team machine task
     */
    public ItemStack getItemStackRepresentationForTeamMachine(TeamMachine teamMachine) {
        ItemStack itemStack = new ItemStack(displayMaterial);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(displayName));

        List<Component> lore = new ArrayList<>();
        for (String line : this.lore) {
            lore.add(Component.text(line));
        }

        lore.add(Component
                .text("Cost: ")
                .color(NamedTextColor.GRAY)
                .append(Component
                        .text(String.format("%d Gold", getCostForTeamMachine(teamMachine)))
                        .color(NamedTextColor.GOLD)
                )
        );
        itemMeta.lore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
