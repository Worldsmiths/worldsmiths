package io.github.worldsmiths.core;

/**
 * Author-owned bitmap font handle. {@link #drawString(String, float, float)} is best-effort and must
 * not throw to fail the game. Call {@link #dispose()} when finished.
 */
public interface Font {

    /**
     * Draws {@code text} at ({@code x}, {@code y}). Best-effort — must not throw to fail the game.
     * Coordinate system / ortho are backend-defined.
     */
    void drawString(String text, float x, float y);

    /** Releases native resources. Safe to call more than once. */
    void dispose();
}
