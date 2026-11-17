package io.github.worldsmiths.core;

/**
 * Portable time facade. Frame delta is also passed to {@link WorldsmithsGame#update(float)}; this
 * service is for games that also want total elapsed time (or a second read of delta).
 */
public interface Clock {

    /** Seconds since the previous frame. */
    float delta();

    /** Seconds since the application started, or since the clock was reset by the backend. */
    float total();
}
