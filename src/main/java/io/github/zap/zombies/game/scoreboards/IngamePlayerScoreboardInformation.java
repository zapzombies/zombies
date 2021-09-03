package io.github.zap.zombies.game.scoreboards;

import lombok.Value;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

@Value
public class IngamePlayerScoreboardInformation {
    Scoreboard bukkitScoreboard;
    IngameScoreboardState ingameState;
    Objective healthObjective;
    Objective zombiesKillObjective;

    SidebarTextWriter sidebarWriter;
    StringFragment zombieKills;
}
