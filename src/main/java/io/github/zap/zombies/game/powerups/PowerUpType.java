package io.github.zap.zombies.game.powerups;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Naming convention: Capitalize words and use dash instead of space (eg: Sb-Slow)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PowerUpType {
    String name();
}
