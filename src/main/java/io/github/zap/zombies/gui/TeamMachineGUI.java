package io.github.zap.zombies.gui;

import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import mc.obliviate.inventory.GUI;
import mc.obliviate.inventory.Icon;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.List;

public class TeamMachineGUI extends GUI {

	private final TeamMachine teamMachine;
	private final ZombiesPlayer zombiesPlayer;

	public TeamMachineGUI(ZombiesPlayer zombiesPlayer, TeamMachine teamMachine) {
		//default rows amount is defined as 6.
		super(zombiesPlayer.getPlayer(), "team-machine-gui", "Team Machine", 1);
		this.teamMachine = teamMachine;
		this.zombiesPlayer = zombiesPlayer;
	}


	/**
	 * Uses magic from TachibanaYui to choose the slots which correspond
	 * to team machine tasks within the team machine GUI
	 */
	@Override
	public void onOpen(InventoryOpenEvent event) {
		List<TeamMachineTask> teamMachineTasks = teamMachine.getShopData().getTeamMachineTasks();
		int num = teamMachineTasks.size();

		if (num > 0) {
			int width = (int) Math.ceil(Math.sqrt(num));
			int height = (int) Math.ceil((double) num / width);
			int remainderLine = Math.min(6, height) / 2;
			// this is the first line offset
			int offset = (height <= 4) ? 1 : 0;
			// If the height go higher than 6 we need to change our calculation
			if (height > 6) {
				width = (int) Math.ceil((double) num / 6);
			}
			int finalLine = num % width;
			if (finalLine == 0) {
				finalLine = width;
			}

			int guiSize = /*9 **/ Math.min(6, height + 2);
			setSize(guiSize);

			int index = 0;

			for (int h = 0; h < height; h++) {
				int lineCount = (h == remainderLine) ? finalLine : width;
				for (int w = 0; w < lineCount && index < num; w++) {
					int slot = (18 * w + 9) / (2 * lineCount);
					int pos = (h + offset) * 9 + slot;

					TeamMachineTask teamMachineTask = teamMachineTasks.get(index);

					Icon icon = new Icon(teamMachineTask.getItemStackRepresentationForTeamMachine(teamMachine));

					icon.onClick(e -> {
						if (teamMachineTask.execute(teamMachine, teamMachine.getArena(), zombiesPlayer)) {
							Sound sound = Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.MASTER,
									1.0F, 1.5F);
							Component message = TextComponent.ofChildren(
									player.getPlayer().displayName(),
									Component.text(" purchased " + teamMachineTask.getDisplayName() +
											" from the Team Machine!", NamedTextColor.YELLOW)
							);
							for (Player otherBukkitPlayer : teamMachine.getArena().getWorld().getPlayers()) {
								otherBukkitPlayer.sendMessage(message);
								otherBukkitPlayer.playSound(sound);
							}
							player.closeInventory();

							addItem(e.getSlot(), teamMachineTask.getItemStackRepresentationForTeamMachine(teamMachine)); // update costs


						}
					});

					addItem(pos, icon);


					index++;
				}
			}
		} /*else {
			default row amount already defined in constructor.
			inventory = Bukkit.createInventory(null, 9, Component.text("Team Machine"));
		}
		*/
	}
}
