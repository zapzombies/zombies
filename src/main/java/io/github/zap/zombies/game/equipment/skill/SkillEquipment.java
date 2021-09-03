package io.github.zap.zombies.game.equipment.skill;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.skill.SkillData;
import io.github.zap.zombies.game.data.equipment.skill.SkillLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a skill
 * @param <D> The skill data type
 * @param <L> The skill level type
 */
public abstract class SkillEquipment<D extends SkillData<L>, L extends SkillLevel> extends UpgradeableEquipment<D, L> {

    boolean usable = true;

    public SkillEquipment(ZombiesArena arena, ZombiesPlayer player, int slot, D skillData) {
        super(arena, player, slot, skillData);
    }

    @Override
    public void onRightClick(@NotNull Action action) {
        super.onRightClick(action);
        if (usable) {
            usable = false;

            final int[] timeRemaining = {getEquipmentData().getDelay()};
            ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text(
                    getEquipmentData().getFormattedDisplayName(getPlayer(), getLevel()),
                    NamedTextColor.RED
            ));
            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(timeRemaining[0]);
            setRepresentingItemStack(itemStack);

            execute();

            getArena().runTaskTimer(20L, 20L, () -> {
                if (--timeRemaining[0] == 0) {
                    setRepresentingItemStack(getEquipmentData().createItemStack(getPlayer(), getLevel()));
                    usable = true;
                } else {
                    itemStack.setAmount(timeRemaining[0]);
                }
            });
        }
    }

    /**
     * Executes the skill
     */
    protected abstract void execute();

}
