package io.github.worldsmiths.core;

/**
 * Portable input facade: keyboard poll plus a primary pointer. No backend types appear in this
 * contract; games never import LibGDX for input.
 */
public interface Input {

    /** Whether {@code key} is currently held down. */
    boolean isKeyDown(Key key);

    /** Whether the primary pointer is currently down. */
    boolean isTouched();

    /** Whether the primary pointer went down this frame. */
    boolean justTouched();

    /** Primary pointer X in screen pixels (origin top-left, matching common touch APIs). */
    int getX();

    /** Primary pointer Y in screen pixels (origin top-left). */
    int getY();
}
