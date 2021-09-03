package io.github.zap.zombies.game.scoreboards;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a lower level way to write to the scoreboard sidebar by modifying and retrieving each line of the sidebar
 * objectives
 */
public class SidebarLineProvider {
    public static final ChatColor[] TEXT_FORMATTING = ChatColor.values();

    // Pretty magic code doc later
    int entryIndex = TEXT_FORMATTING.length + 1;
    private String incrementEntry() {
        entryIndex++;
        List<String> entries = new ArrayList<>();
        for(int i = 0; i < 40; i++) {
            var digit = digitAt(entryIndex, i, TEXT_FORMATTING.length + 1);
            entries.add( digit == 0 ? "" : "" + TEXT_FORMATTING[digit - 1]);

            // Check for early break. If the number smallest number have i + 1 in length bigger than entryIndex
            if(Math.pow(TEXT_FORMATTING.length + 1, i + 1) > entryIndex) {
                break;
            }
        }

        // Since we add in from right to left (easier with the base calculation) we have to reverse our string to get
        // the desired output
        Collections.reverse(entries);
        return String.join("", entries);
    }

    // Considered making a number utils class
    private static int digitAt(int num, int index, int base) {
        return (int)(num / Math.pow(base, index)) % base;
    }


    @Getter
    private final Scoreboard scoreboard;

    @Getter
    private final Objective objective;

    private final List<Team> teams = new ArrayList<>();

    @Getter()
    @Setter(AccessLevel.PRIVATE)
    private int lineCount;

    public SidebarLineProvider(Scoreboard scoreboard, Objective objective) {
        this.scoreboard = scoreboard;
        this.objective = objective;
    }

    /**
     * retrieve content from a line
     * @param lineIndex the position of the line to retrieve data from
     * @return a String contains the line content
     */
    public String getLine(int lineIndex) {
        if(lineIndex < getLineCount()) {
            return teams.get(lineIndex).getPrefix();
        } else {
            return null;
        }
    }

    /**
     * Clear content from all lines and removes them from the scoreboard objective
     */
    public void clearLines() {
        teams.forEach(x -> x.getEntries().stream().findFirst().ifPresent(scoreboard::resetScores));
    }

    /**
     * Write contents to a line
     * @param lineIndex the position of the line to write
     * @param content the content to write
     */
    public void setLine(int lineIndex, String content) {
        // Generating lines with placeholder string
        while(getLineCount() <= lineIndex) {
            if(teams.size() > getLineCount()) {
                teams.get(getLineCount()).getEntries().stream().findFirst()
                        .ifPresent(s -> getObjective().getScore(s).setScore(getLineCount()));
            } else {
                var team = getScoreboard().registerNewTeam(getObjective().getName() + "-" + getLineCount());
                var entry = incrementEntry();
                team.addEntry(entry);
                getObjective().getScore(entry).setScore(-1 -getLineCount());
                teams.add(team);
            }

            setLineCount(getLineCount() + 1);
        }

        // set the requested line
        teams.get(lineIndex).setPrefix(content);
    }

    /**
     * Release all the resources used by this object
     */
    public void dispose() {
        teams.forEach(Team::unregister);
    }
}

