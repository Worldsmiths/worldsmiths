package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.Clock;

/** GDX-backed {@link Clock}. Not part of the author-facing API — backends assemble this. */
final class GdxClock implements Clock {

    @Override
    public float delta() {
        return Gdx.graphics.getDeltaTime();
    }

    @Override
    public float total() {
        return GdxClockState.totalSeconds();
    }
}
