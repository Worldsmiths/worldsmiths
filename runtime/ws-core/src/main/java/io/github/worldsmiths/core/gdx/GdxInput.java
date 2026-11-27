package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.DeviceState;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Key;

/**
 * GDX-backed {@link Input}. Keys read from the shared {@link DeviceState} filled each frame by
 * {@link GdxActionDevices#sync()} (same port as Actions). Pointer coords / touch edges stay on
 * {@code Gdx.input}. Not part of the author-facing API — backends assemble this.
 */
final class GdxInput implements Input {
    private final DeviceState devices;

    GdxInput(DeviceState devices) {
        if (devices == null) {
            throw new IllegalArgumentException("DeviceState must not be null");
        }
        this.devices = devices;
    }

    @Override
    public boolean isKeyDown(Key key) {
        return devices.isKeyDown(key);
    }

    @Override
    public boolean isTouched() {
        return Gdx.input.isTouched();
    }

    @Override
    public boolean justTouched() {
        return Gdx.input.justTouched();
    }

    @Override
    public int getX() {
        return Gdx.input.getX();
    }

    @Override
    public int getY() {
        return Gdx.input.getY();
    }
}
