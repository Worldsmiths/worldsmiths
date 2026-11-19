package io.github.worldsmiths.core;

/**
 * Author-owned one-shot sound handle. {@link #play()} and {@link #stop()} are best-effort and must
 * not throw to fail the game. Call {@link #dispose()} when finished.
 */
public interface Sound {

    /** Starts playback. Best-effort — must not throw to fail the game. */
    void play();

    /** Stops playback. Best-effort — must not throw to fail the game. */
    void stop();

    /** Releases native resources. Safe to call more than once. */
    void dispose();
}
