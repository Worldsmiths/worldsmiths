package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.DeviceState;
import io.github.worldsmiths.core.Key;
import org.junit.jupiter.api.Test;

/**
 * HLD: GdxInput polls keys from the shared {@link DeviceState} filled by {@link GdxActionDevices}
 * (same port as Actions). Headless — no Gdx.input for key polls.
 */
class GdxInputDeviceStateTest {

    @Test
    void isKeyDownReadsSharedDeviceState() {
        DeviceState devices = new DeviceState();
        GdxInput input = new GdxInput(devices);

        assertFalse(input.isKeyDown(Key.W));
        devices.setKeyDown(Key.W, true);
        assertTrue(input.isKeyDown(Key.W));
        devices.setKeyDown(Key.W, false);
        assertFalse(input.isKeyDown(Key.W));
    }
}
