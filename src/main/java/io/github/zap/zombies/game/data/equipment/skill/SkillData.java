package io.github.zap.zombies.game.data.equipment.skill;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for a skill
 * @param <L> The level type of the skill
 */
public abstract class SkillData<L extends SkillLevel> extends EquipmentData<L> {

    @Getter
    private int delay;

    public SkillData(@NotNull String type, @NotNull String name, @NotNull String displayName,
                     @NotNull Material material, @NotNull List<String> lore, @NotNull List<L> levels, int delay) {
        super(type, name, displayName, material, lore, levels);

        this.delay = delay;
    }

    protected SkillData() {

    }

    @Override
    public @NotNull TextColor getDefaultChatColor() {
        return NamedTextColor.AQUA;
    }

    @Override
    public @NotNull String getEquipmentObjectGroupType() {
        return EquipmentObjectGroupType.SKILL.name();
    }

}
