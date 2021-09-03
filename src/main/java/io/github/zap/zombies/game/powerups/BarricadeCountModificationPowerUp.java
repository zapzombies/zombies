package io.github.zap.zombies.game.powerups;

import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.data.powerups.BarricadeCountModificationPowerUpData;
import io.github.zap.zombies.game.data.powerups.ModifierMode;
import io.github.zap.zombies.game.util.MathUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * Modify the number of barricade block on a window using a function f(x) = x * multiplier + amount
 */
@PowerUpType(name = "Barricade-Count-Modification")
public class BarricadeCountModificationPowerUp extends PowerUp{
    public BarricadeCountModificationPowerUp(BarricadeCountModificationPowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public BarricadeCountModificationPowerUp(BarricadeCountModificationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    @Override
    public void activate() {
        var cData = (BarricadeCountModificationPowerUpData) getData();

        getArena().getMap().getRooms().stream().flatMap(x -> x.getWindows().stream())
                .filter(x -> cData.isAffectAll() || x.inRange(getDropLocation().toVector(), cData.getAffectedRange()))
                .forEach(x -> modWindow(x, cData));
        getArena().getPlayerMap().forEach((l,r) -> r.addCoins(((BarricadeCountModificationPowerUpData) getData())
                .getRewardGold(), Component.text(getData().getDisplayName(), NamedTextColor.GOLD)));
    }

    private void modWindow(WindowData windowData, BarricadeCountModificationPowerUpData data) {
        var i = data.getModifierMode() == ModifierMode.RELATIVE ?
                windowData.getCurrentIndexProperty().getValue(getArena()) + 1 :
                windowData.getFaceVectors().size();


        var valToChange = (int) MathUtils.clamp(i * data.getMultiplier() + data.getAmount(), 0, windowData.getVolume());
        windowData.getCurrentIndexProperty().setValue(getArena(), valToChange - 1);

        // TODO: Change this after @Steank Change Breaking pattern
        for(int s = 0; s < windowData.getVolume(); s++) {
            BlockData blockData;
            if(s <= valToChange - 1) {
                blockData = Bukkit.createBlockData(windowData.getRepairedData().get(s));
            }
            else {
                blockData = Bukkit.createBlockData(Material.AIR);
            }

            WorldUtils.getBlockAt(getDropLocation().getWorld(), windowData.getFaceVectors().get(s)).setBlockData(blockData);
        }
    }
}
