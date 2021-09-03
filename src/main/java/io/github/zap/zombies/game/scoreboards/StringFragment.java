package io.github.zap.zombies.game.scoreboards;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represent a segment of string to render in SidebarTextWriter
 */
public class StringFragment implements ITextFragment {
    @Getter
    public String value;

    private final Set<TextWriter> writers = new HashSet<>();

    /**
     * Create a new instance of TextFragment
     * @param value the initial value
     */
    public StringFragment(String value) {
        this.value = value;
    }

    /**
     * Create a new instance of TextFragment
     */
    public StringFragment() {
        this("");
    }

    /**
     * Set a new value of this TextFragment
     * @param newValue a string represent the new value to set
     */
    public void setValue(String newValue) {
        if(!getValue().equals(newValue)) {
            value = newValue;
             getWriters().forEach(x -> x.onTextFragmentChanged(this));
        }
    }

    /**
     * Get the computed text
     */
    @Override
    public String getComputedText() {
        return getValue();
    }

    @Override
    public Iterable<TextWriter> getWriters() {
        return writers;
    }

    @Override
    public void addWriter(TextWriter writer) {
        writers.add(writer);
    }

    @Override
    public void removeWriter(TextWriter writer) {
        writers.remove(writer);
    }
}
