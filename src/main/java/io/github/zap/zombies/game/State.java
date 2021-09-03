package io.github.zap.zombies.game;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Defines a state, which is a value that can have modifiers applied to it. These modifiers should, in the order they
 * are applied, return modified copies of the original baseValue.
 * @param <T> The type of object held by this State instance
 */
public class State<T> {
    public interface Modifier<T> {
        /**
         * Applies a modification to the base value. This should not change baseValue itself; rather, it must return a
         * new, changed copy of baseValue.
         *
         * Modifiers are expected to be stateless and repeatable; that is, for any value X, they should always return
         * value Y no matter when they are called. This allows for value caching.
         * @param value baseValue, or a copy of baseValue that has already been modified
         * @return A copy of baseValue, with modifications applied
         */
        @Nullable T modify(@Nullable T value);
    }

    @Getter
    private final T baseValue;

    private T cache;
    private boolean cacheValid = true;

    private final Map<String, Modifier<T>> modifierMap = new LinkedHashMap<>();

    public State(@Nullable T baseValue) {
        this.baseValue = baseValue;
        cache = baseValue;
    }

    /**
     * Registers a modifier with this State object. If a modifier with the provided name already exists, it will be
     * replaced. Otherwise, the modifier will be added to the end of the current modifier map (modifiers are called
     * according to the order they were first added; modifiers added later will be called later).
     * @param name The name of the modifier
     * @param modifier The modifier itself
     */
    public void registerModifier(@NotNull String name, @NotNull Modifier<T> modifier) {
        Objects.requireNonNull(name, "name cannot be null!");
        Objects.requireNonNull(modifier, "modifier cannot be null!");
        modifierMap.put(name, modifier);
        cacheValid = false;
    }

    /**
     * Removes a named modifier from the internal map, if it exists.
     * @param name The name of the modifier, which may not exist
     */
    public void removeModifier(@NotNull String name) {
        Objects.requireNonNull(name, "name cannot be null!");
        cacheValid = modifierMap.remove(name) == null;
    }

    /**
     * Returns true if this State has a modifier with the given name. Returns false otherwise.
     */
    public boolean hasModifier(@NotNull String name) {
        Objects.requireNonNull(name, "name cannot be null");
        return  modifierMap.containsKey(name);
    }

    /**
     * Gets the value after applying any present modifiers to it. This may be the same object initially assigned to
     * baseValue; if it is mutable, modifying it is possible but not recommended and may result in undefined
     * behavior.
     * @return The value, after applying modifiers
     */
    public @Nullable T getValue() {
        if(cacheValid) {
            return cache;
        }
        else {
            T result = baseValue;
            for(Modifier<T> modifier : modifierMap.values()) {
                result = modifier.modify(result);
            }

            cache = result;
            cacheValid = true;
        }

        return cache;
    }
}