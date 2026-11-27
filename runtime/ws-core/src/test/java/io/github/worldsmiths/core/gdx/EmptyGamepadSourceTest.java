package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.Controls;
import io.github.worldsmiths.core.DefaultActions;
import io.github.worldsmiths.core.DeviceState;
import io.github.worldsmiths.core.PadButton;
import org.junit.jupiter.api.Test;

/**
 * AC9 / V9: EmptyGamepadSource (iOS soft-absent closeout; no Controllers forceLink / no HW AC) —
 * bind succeeds; poll stays false.
 */
class EmptyGamepadSourceTest {

    @Test
    void bindSucceedsAndPollStaysFalseWithEmptyGamepadSource() {
        DeviceState devices = new DeviceState();
        GamepadSource pads = EmptyGamepadSource.INSTANCE;
        assertFalse(pads.isPresent());
        assertFalse(pads.isButtonDown(PadButton.A));

        for (PadButton button : PadButton.values()) {
            devices.setPadButtonDown(button, pads.isButtonDown(button));
        }

        DefaultActions actions = new DefaultActions(devices);
        actions.bind("confirm", Controls.gamepadButton(PadButton.A));
        actions.tick();

        assertFalse(actions.isDown("confirm"));
        assertFalse(actions.justPressed("confirm"));
    }

    @Test
    void padEmulatedKeysAreSuppressedWhenPadPresent() {
        assertTrue(GdxActionDevices.shouldSuppressPadEmulatedKey(io.github.worldsmiths.core.Key.UP, true));
        assertTrue(GdxActionDevices.shouldSuppressPadEmulatedKey(io.github.worldsmiths.core.Key.LEFT, true));
        assertFalse(GdxActionDevices.shouldSuppressPadEmulatedKey(io.github.worldsmiths.core.Key.UP, false));
        assertFalse(GdxActionDevices.shouldSuppressPadEmulatedKey(io.github.worldsmiths.core.Key.W, true));
    }
}
