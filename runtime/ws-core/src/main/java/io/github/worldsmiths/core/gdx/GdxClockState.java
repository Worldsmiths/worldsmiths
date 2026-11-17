package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.utils.TimeUtils;

/**
 * Process-wide start mark for {@link GdxClock#total()}. Package-private helper so the clock stays
 * a thin Gdx wrapper without exposing mutable state on the public facade.
 */
final class GdxClockState {
    private static final long START_NANOS = TimeUtils.nanoTime();

    private GdxClockState() {}

    static float totalSeconds() {
        return (TimeUtils.nanoTime() - START_NANOS) * 1e-9f;
    }
}
