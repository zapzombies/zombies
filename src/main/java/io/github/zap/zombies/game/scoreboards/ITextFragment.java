package io.github.zap.zombies.game.scoreboards;

public interface ITextFragment{
    String getComputedText();
    Iterable<TextWriter> getWriters();
    void addWriter(TextWriter writer);
    void removeWriter(TextWriter writer);

}