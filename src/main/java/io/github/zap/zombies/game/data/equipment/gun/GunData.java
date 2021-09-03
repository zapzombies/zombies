package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.zombies.game.data.equipment.UltimateableData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a gun
 * @param <L> The gun level type
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public abstract class GunData<L extends GunLevel> extends UltimateableData<L> {

    private Sound sound = null;

    private static final transient String UNCHANGED_FORMAT = ChatColor.DARK_GRAY + " \u25fc " + ChatColor.GRAY + "%s: "
            + ChatColor.GREEN + "%s";

    private static final transient String CHANGED_FORMAT = ChatColor.DARK_GRAY + " \u25fc " + ChatColor.GRAY + "%s: "
            + ChatColor.DARK_GRAY + "%s \u2794 " + ChatColor.GREEN + "%s";

    public GunData(@NotNull String type, @NotNull String name, @NotNull String displayName, @NotNull Material material,
                   @NotNull List<String> lore, @NotNull List<L> levels) {
        super(type, name, displayName, material, lore, levels);
    }

    protected GunData() {

    }

    @Override
    public @NotNull List<String> getLore(@NotNull Player player, int level) {
        List<String> lore = super.getLore(player, level);

        lore.add("");
        lore.addAll(getStatsLore(level));
        lore.add("");

        lore.add(ChatColor.YELLOW + "Left Click"
                + " " + ChatColor.GRAY + "to Reload"
                + ".");
        lore.add(ChatColor.YELLOW + "Right Click"
                + " " + ChatColor.GRAY + "to Shoot"
                + ".");

        return lore;
    }

    private List<String> getStatsLore(int level) {
        List<String> statsLore = new ArrayList<>();
        GunLevel current = getLevels().get(level);

        // TODO: make this not bad code
        if (level > 0) {
            GunLevel previous = getLevels().get(level - 1);

            if (previous.getDamage() == current.getDamage()) {
                statsLore.add(String.format(UNCHANGED_FORMAT, "Damage", current.getDamage() + " HP"));
            } else {
                statsLore.add(String.format(CHANGED_FORMAT, "Damage", previous.getDamage() + " HP", current.getDamage()
                        + " HP"));
            }
            if (previous.getAmmo() == current.getAmmo()) {
                statsLore.add(String.format(UNCHANGED_FORMAT, "Ammo", current.getAmmo()));
            } else {
                statsLore.add(String.format(CHANGED_FORMAT, "Ammo", previous.getAmmo(), current.getAmmo()));
            }
            if (previous.getClipAmmo() == current.getClipAmmo()) {
                statsLore.add(String.format(UNCHANGED_FORMAT, "Clip Ammo", current.getClipAmmo()));
            } else {
                statsLore.add(String.format(CHANGED_FORMAT, "Clip Ammo", previous.getClipAmmo(), current.getClipAmmo()));
            }
            if (previous.getFireRate() == current.getFireRate()) {
                statsLore.add(String.format(UNCHANGED_FORMAT, "Fire Rate",
                        TimeUtil.convertTicksToSecondsString(current.getFireRate())));
            } else {
                statsLore.add(String.format(CHANGED_FORMAT, "Fire Rate",
                        TimeUtil.convertTicksToSecondsString(previous.getFireRate()),
                        TimeUtil.convertTicksToSecondsString(current.getFireRate())));
            }
            if (previous.getReloadRate() == current.getReloadRate()) {
                statsLore.add(String.format(UNCHANGED_FORMAT, "Reload Rate",
                        TimeUtil.convertTicksToSecondsString(current.getReloadRate())));
            } else {
                statsLore.add(String.format(CHANGED_FORMAT, "Reload Rate",
                        TimeUtil.convertTicksToSecondsString(previous.getReloadRate()),
                        TimeUtil.convertTicksToSecondsString(current.getReloadRate())));
            }
        } else {
            statsLore.add(String.format(UNCHANGED_FORMAT, "Damage", current.getDamage() + " HP"));
            statsLore.add(String.format(UNCHANGED_FORMAT, "Ammo", current.getAmmo()));
            statsLore.add(String.format(UNCHANGED_FORMAT, "Clip Ammo", current.getClipAmmo()));
            statsLore.add(String.format(UNCHANGED_FORMAT, "Fire Rate",
                    TimeUtil.convertTicksToSecondsString(current.getFireRate())));
            statsLore.add(String.format(UNCHANGED_FORMAT, "Reload Rate",
                    TimeUtil.convertTicksToSecondsString(current.getReloadRate())));
        }

        return statsLore;
    }

    @Override
    public @NotNull TextColor getDefaultChatColor() {
        return NamedTextColor.GOLD;
    }

    @Override
    public @NotNull String getEquipmentObjectGroupType() {
        return EquipmentObjectGroupType.GUN.name();
    }

}
