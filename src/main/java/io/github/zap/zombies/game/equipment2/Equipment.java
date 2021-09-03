package io.github.zap.zombies.game.equipment2;

import io.github.zap.arenaapi.hotbar2.HotbarManager;
import io.github.zap.arenaapi.hotbar2.HotbarObjectBase;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Equipment extends HotbarObjectBase {

    private final Map<UUID, ZombiesPlayer> playerMap;

    private final List<EquipmentLevel> levels;

    private EquipmentLevel level;

    private int levelIndex = 0;

    public Equipment(@NotNull HotbarManager hotbarManager, boolean selected,
                     @NotNull Map<UUID, ZombiesPlayer> playerMap, @NotNull List<EquipmentLevel> levels) {
        super(hotbarManager, null, selected);

        this.playerMap = playerMap;
        this.levels = levels;
        this.level = levels.get(levelIndex);
    }

    public @NotNull List<EquipmentLevel> getLevels() {
        return new ArrayList<>(levels);
    }

    public @NotNull EquipmentLevel getLevel() {
        return level;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    public void upgrade() {
        if (levelIndex < levels.size()) {
            level = levels.get(++levelIndex);
            currentStack = level.features().get(level.visualFeatureIndex()).getVisual(this);
            redraw();
        }
        else throw new IllegalStateException("The equipment is already at its max level!");
    }

    public void downgrade() {
        if (levelIndex > 0) {
            level = levels.get(--levelIndex);
            currentStack = level.features().get(level.visualFeatureIndex()).getVisual(this);
            redraw();
        }
        else throw new IllegalStateException("The equipment is already at level " + levelIndex + "!");
    }

    @Override
    protected void onLeftClick(@NotNull PlayerInteractEvent event) {
        ZombiesPlayer player = playerMap.get(event.getPlayer().getUniqueId());
        if (!player.isInGame()) {
            return;
        }

        int visualIndex = level.visualFeatureIndex();
        level.features().get(visualIndex).onLeftClick(this, player, null);
        for (int i = 0; i < level.features().size(); i++) {
            if (i == visualIndex) {
                continue;
            }
            level.features().get(i).onLeftClick(this, player, null);
        }
    }

    @Override
    protected void onRightClick(@NotNull PlayerInteractEvent event) {
        ZombiesPlayer player = playerMap.get(event.getPlayer().getUniqueId());
        if (!player.isInGame()) {
            return;
        }

        int visualIndex = level.visualFeatureIndex();
        level.features().get(visualIndex).onLeftClick(this, player, null);
        for (int i = 0; i < level.features().size(); i++) {
            if (i == visualIndex) {
                continue;
            }
            level.features().get(i).onLeftClick(this, player, null);
        }
    }

    @Override
    protected void onSelected(@NotNull PlayerItemHeldEvent event) {
        ZombiesPlayer player = playerMap.get(event.getPlayer().getUniqueId());
        if (!player.isInGame()) {
            return;
        }

        int visualIndex = level.visualFeatureIndex();
        level.features().get(visualIndex).onLeftClick(this, player, null);
        for (int i = 0; i < level.features().size(); i++) {
            if (i == visualIndex) {
                continue;
            }
            level.features().get(i).onLeftClick(this, player, null);
        }
    }

    @Override
    protected void onDeselected(@NotNull PlayerItemHeldEvent event) {
        ZombiesPlayer player = playerMap.get(event.getPlayer().getUniqueId());
        if (!player.isInGame()) {
            return;
        }

        int visualIndex = level.visualFeatureIndex();
        level.features().get(visualIndex).onLeftClick(this, player, this::changeStack);
        for (int i = 0; i < level.features().size(); i++) {
            if (i == visualIndex) {
                continue;
            }
            level.features().get(i).onLeftClick(this, player, null);
        }
    }

    private void changeStack(@Nullable ItemStack stack) {
        currentStack = stack;
        redraw();
    }

}
