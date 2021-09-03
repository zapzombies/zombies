package io.github.zap.zombies.game.scoreboards;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SidebarDisplayNameTextWriter implements Iterable<ITextFragment>, TextWriter {
    // It seems like there is not limit to the display name
    @Getter
    private final SidebarTextWriter writer;

    private final List<ITextFragment> fragments = new ArrayList<>();

    public SidebarDisplayNameTextWriter(SidebarTextWriter writer) {
        this.writer = writer;
    }

    /**
     * Get the SidebarTextWriter that owns this instance. Does the same thing as getWriter, this method exists to
     * help with semantics
     */
    public SidebarTextWriter body() {
        return getWriter();
    }

    /**
     * Appends an array of fragmented text
     * @param contents an array of objects contains fragmented text or string
     * @return the current SidebarDisplayNameTextWriter object
     */
    public SidebarDisplayNameTextWriter text(Object... contents) {
        for(Object content : contents) {
            if(content instanceof ITextFragment textFragment)
                add(textFragment);
            else
                add(new StringFragment(content.toString()));
        }

        return this;
    }

    /**
     * Appends an array of fragmented text
     * @param fragments an array of TextFragment
     * @return the current SidebarDisplayNameTextWriter object
     */
    public SidebarDisplayNameTextWriter textf(ITextFragment... fragments) {
        for(ITextFragment fragment : fragments)
            add(fragment);
        return this;
    }

    /**
     * Appends an Iterable of fragmented text
     * @param fragments an Iterable of TextFragment
     * @return the current SidebarDisplayNameTextWriter object
     */
    public SidebarDisplayNameTextWriter textf(Iterable<ITextFragment> fragments) {
        fragments.forEach(this::add);
        return this;
    }

    /**
     * Appends an array of text. These items will be converted to TextFragment internally
     * @param contents an array of string
     * @return the current SidebarTextWriter object
     */
    public SidebarDisplayNameTextWriter textc (String... contents) {
        for(String content : contents)
            add(new StringFragment(content));
        return this;
    }

    @Override
    public void onTextFragmentChanged(ITextFragment fragment) {
        if(getWriter().isAutoUpdate() && fragments.contains(fragment)) getWriter().update();
    }


    public void update() {
        StringBuilder bd = new StringBuilder();
        for(ITextFragment fragment : fragments)
            bd.append(fragment.getComputedText());
        getWriter().getObjective().setDisplayName(bd.toString());
    }




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
        if(getWriter().isAutoUpdate()) getWriter().update();
    }

    /**
     * Insert a TextFragment object. Note: SideBarTextWriter often add additional TextFragments help with styling
     * @param index the index to insert
     * @param fragment the text fragment to add
     */
    public void add(int index, ITextFragment fragment) {
        fragments.add(index, fragment);
        fragment.addWriter(this);
        if(getWriter().isAutoUpdate()) getWriter().update();
    }

    /**
     * Remove a text fragment
     * @param fragment the text fragment to remove
     */
    public void remove(ITextFragment fragment) {
        fragments.remove(fragment);
        fragment.removeWriter(this);
        if(getWriter().isAutoUpdate()) getWriter().update();
    }

    /**
     * Remove a text fragment by its index. Note: SideBarTextWriter often add additional TextFragments help with styling
     * @param index a zero-based index represent the text fragment position to remove
     */
    public void remove(int index) {
        fragments.get(index).removeWriter(this);
        fragments.remove(index);
        if(getWriter().isAutoUpdate()) getWriter().update();
    }

    /**
     * Removes all text fragments stored in this instance
     */
    public void clear() {
        fragments.clear();
        if(getWriter().isAutoUpdate()) getWriter().update();
    }
}

