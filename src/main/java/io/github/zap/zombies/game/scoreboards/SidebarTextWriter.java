package io.github.zap.zombies.game.scoreboards;

import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.RandomStringUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A wrapper that render text to a sidebar objective
 */
public class SidebarTextWriter implements Iterable<ITextFragment>, TextWriter {
    // implement non-reduced-flicker for 128 + 40 chars (currently 64)
    public static final int MAX_CHAR_REDUCE_FLICKER = 64;
    public static final int MAX_CHAR = 168;

    @Getter
    @Setter
    private boolean autoUpdate;

    @Getter
    private final SidebarDisplayNameTextWriter titleWriter;

    @Getter
    private final Objective objective;

    @Getter
    private final Scoreboard scoreboard;

    @Getter
    private final boolean isGenerated;

    @Getter
    @Setter
    private WrapMode textWrapping = WrapMode.CHARACTER;

    private final List<ITextFragment> fragments = new ArrayList<>();

    private final SidebarLineProvider lineProvider;

    /**
     * Create a new SidebarTextWriter to render to an existing sidebar objective
     * @param scoreboard the objective parent
     * @param objective the target objective to render to
     */
    public static SidebarTextWriter source (Scoreboard scoreboard, Objective objective) {
        return new SidebarTextWriter(scoreboard, objective, false);
    }

    /**
     * Create a new SidebarTextWriter to render to a new sidebar objective. This objective will be created internally
     * @param scoreboard the scoreboard for the new objective to register
     * @param title the objective display name
     */
    public static SidebarTextWriter create (Scoreboard scoreboard, String title) {
        var name = "SBTW-" + RandomStringUtils.random(8, "1234567890abcdef");
        return new SidebarTextWriter(scoreboard, scoreboard.registerNewObjective(name, "dummy", title), true);
    }


    private SidebarTextWriter(Scoreboard scoreboard, Objective objective, boolean isGenerated) {
        this.scoreboard = scoreboard;
        this.objective = objective;
        this.titleWriter = new SidebarDisplayNameTextWriter(this);
        this.titleWriter.textc(objective.getDisplayName());
        this.isGenerated = isGenerated;
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.lineProvider = new SidebarLineProvider(scoreboard, objective);
    }


    /**
     * Get the SidebarDisplayNameTextWriter controlled by this instance. This method is the same as getTitleWriter this
     * method exists to help with semantics
     */
    public SidebarDisplayNameTextWriter title() {
        return getTitleWriter();
    }

    /**
     * the maximum number of character can be display in a single line
     * @return the viewportWidth of the sidebar scoreboard
     */
    public int getViewPortWidth() {
        // Implement non-reduce-flicker mode for MAX_CHAR
        return MAX_CHAR_REDUCE_FLICKER;
    }

    /**
     * Release all the resources used by this object
     */
    public void dispose() {
        lineProvider.dispose();
        if(isGenerated()) objective.unregister();
    }

    /* Overloads of adding text fragment */
    // Weak type, other member might refuse this so I will not make other methods depend on this one
    // But this allow seamless concat so I will do this
    /**
     * Appends an array of fragmented text
     * @param contents an array of objects contains fragmented text or string
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter text(Object... contents) {
        for(Object content : contents) {
            if(content instanceof ITextFragment textFragment)
                add(textFragment);
            else
                add(new StringFragment(content.toString()));
        }

        return this;
    }

    /**
     * Appends an array of fragmented text then insert a newline
     * @param contents an array of objects contains fragmented text or string
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter line(Object... contents)  {
        return text(contents).line();
    }

    /**
     * Appends an array of fragmented text
     * @param fragments an array of TextFragment
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter textf(ITextFragment... fragments) {
        for(ITextFragment fragment : fragments)
            add(fragment);
        return this;
    }

    /**
     * Appends an Iterable of fragmented text
     * @param fragments an Iterable of TextFragment
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter textf(Iterable<ITextFragment> fragments) {
        fragments.forEach(this::add);
        return this;
    }

    /**
     * Appends an array of text. These items will be converted to TextFragment internally
     * @param contents an array of string
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter textc (String... contents) {
        for(String content : contents)
            add(new StringFragment(content));
        return this;
    }

    /**
     * Appends an Iterable of string. These items will be converted to TextFragment internally
     * @param contents an Iterable of string
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter textc(Iterable<String> contents) {
        contents.forEach(x -> add(new StringFragment(x)));
        return this;
    }

    /**
     * Insert a newline
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter line() {
        add(new StringFragment("\n"));
        return this;
    }

    /**
     * Appends an array of fragmented text then insert a newline
     * @param fragments an array of TextFragment
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter linef(ITextFragment... fragments) {
        return textf(fragments).line();
    }

    /**
     * Appends an Iterable of fragmented text then insert a newline
     * @param fragments an Iterable of TextFragment
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter linef(Iterable<ITextFragment> fragments) {
        return textf(fragments).line();
    }

    /**
     * Appends an array of text then insert a newline. These items will be converted to TextFragment internally
     * @param contents an array of string
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter linec (String... contents) {
        return textc(contents).line();
    }

    /**
     * Appends an Iterable of string then insert a newline. These items will be converted to TextFragment internally
     * @param contents an Iterable of string
     * @return the current SidebarTextWriter object
     */
    public SidebarTextWriter linec(Iterable<String> contents) {
        return textc(contents).line();
    }

    /* Logic */
    /**
     * Used by TextFragment to inform this object that its state has been changed
     * @param fragment the calling fragment
     */
    @Override
    public void onTextFragmentChanged(ITextFragment fragment) {
        if(isAutoUpdate() && fragments.contains(fragment)) update();
    }

    /**
     * Calculate Text Fragments positions and render to the sidebar objective
     */
    public void update() {
        titleWriter.update();
        pushChangesToScoreboard(fillBuffer());
    }

    private void wrapCharacter(ArrayList<String> lines, StringBuilder sb, String valLine) {
        String remainder = valLine;

        while(sb.length() + remainder.length() > 64) {
            int remainingLength = getViewPortWidth() - sb.length();
            sb.append(remainder, 0, remainingLength);
            lines.add(sb.toString());
            sb.setLength(0);
            remainder = remainder.substring(remainingLength);
        }

        sb.append(remainder);
    }

    private ArrayList<String> fillBuffer() {
        var lines = new ArrayList<String>();
        StringBuilder bd = new StringBuilder();
        for(ITextFragment frag : this) {
            String[] value = frag.getComputedText().split("\n", -1);

            // Iterate through each line of the text fragment to perform text wrapping
            for (int i = 0; i < value.length; i++) {
                String valLine = value[i];
                if(bd.length() + valLine.length() < getViewPortWidth()) {
                    bd.append(valLine);
                } else {
                    if(getTextWrapping() == WrapMode.CHARACTER) {
                        wrapCharacter(lines, bd, valLine);
                    } else {
                        // TODO: Implemented other wrapping mode, for now this setting will behave the same as character wrapping
                        wrapCharacter(lines, bd, valLine);
                    }
                }

                // The last line will not suffix with a newline
                // Example "Hello\nWorld" have 2 lines but we only print once
                if (i + 1 != value.length) {
                    lines.add(bd.toString());
                    bd.setLength(0);
                }
            }
        }

        // add the remaining characters in StringBuilder's buffer
        lines.add(bd.toString());

        return lines;
    }

    private void pushChangesToScoreboard(ArrayList<String> lines) {
        if(lines.size() < lineProvider.getLineCount())
            lineProvider.clearLines();

        for(int i = 0; i < lines.size(); i++) {
            if(lineProvider.getLine(i) == null || !lineProvider.getLine(i).equals(lines.get(i))) {
                lineProvider.setLine(i, lines.get(i));
            }
        }
    }

    /* Collection operations */
    @NotNull
    @Override
    public Iterator<ITextFragment> iterator() {
        return fragments.iterator();
    }

    /**
     * Add a TextFragment object. Use the text() textf() or textc() for convenience
     * @param fragment the fragment to add
     */
    public void add(ITextFragment fragment) {
        fragments.add(fragment);
        fragment.addWriter(this);
        if(isAutoUpdate()) update();
    }

    /**
     * Insert a TextFragment object. Note: SideBarTextWriter often add additional TextFragments help with styling
     * @param index the index to insert
     * @param fragment the text fragment to add
     */
    public void add(int index, ITextFragment fragment) {
        fragments.add(index, fragment);
        fragment.addWriter(this);
        if(isAutoUpdate()) update();
    }

    /**
     * Remove a text fragment
     * @param fragment the text fragment to remove
     */
    public void remove(ITextFragment fragment) {
        fragments.remove(fragment);
        fragment.removeWriter(this);
        if(isAutoUpdate()) update();
    }

    /**
     * Remove a text fragment by its index. Note: SideBarTextWriter often add additional TextFragments help with styling
     * @param index a zero-based index represent the text fragment position to remove
     */
    public void remove(int index) {
        fragments.get(index).removeWriter(this);
        fragments.remove(index);
        if(isAutoUpdate()) update();
    }

    /**
     * Removes all text fragments stored in this instance
     */
    public void clear() {
        fragments.clear();
        if(isAutoUpdate()) update();
    }
}

