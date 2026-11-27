package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import io.github.worldsmiths.core.DeviceState;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.PadButton;
import io.github.worldsmiths.core.PointerButton;

/**
 * Syncs keyboard, pointer buttons, and gamepad into a shared {@link DeviceState} each frame.
 * Controllers stay behind {@link GamepadSource}; author code never sees GDX types.
 */
final class GdxActionDevices {
    private final DeviceState devices;
    private final GamepadSource gamepad;

    GdxActionDevices(DeviceState devices, GamepadSource gamepad) {
        if (devices == null) {
            throw new IllegalArgumentException("DeviceState must not be null");
        }
        if (gamepad == null) {
            throw new IllegalArgumentException("GamepadSource must not be null");
        }
        this.devices = devices;
        this.gamepad = gamepad;
    }

    DeviceState devices() {
        return devices;
    }

    GamepadSource gamepad() {
        return gamepad;
    }

    /**
     * Polls GDX Input + {@link GamepadSource} into {@link DeviceState}. When a pad is present,
     * suppresses Android pad-emulated overlapping arrow keycodes (HLD R3).
     */
    void sync() {
        boolean padPresent = gamepad.isPresent();
        for (Key key : Key.values()) {
            if (shouldSuppressPadEmulatedKey(key, padPresent)) {
                devices.setKeyDown(key, false);
                continue;
            }
            int code = GdxKeys.code(key);
            devices.setKeyDown(key, code >= 0 && Gdx.input.isKeyPressed(code));
        }
        devices.setPointerButtonDown(
                PointerButton.PRIMARY,
                Gdx.input.isButtonPressed(Buttons.LEFT) || Gdx.input.isTouched());
        devices.setPointerButtonDown(
                PointerButton.SECONDARY, Gdx.input.isButtonPressed(Buttons.RIGHT));
        devices.setPointerButtonDown(
                PointerButton.MIDDLE, Gdx.input.isButtonPressed(Buttons.MIDDLE));
        for (PadButton button : PadButton.values()) {
            devices.setPadButtonDown(button, gamepad.isButtonDown(button));
        }
    }

    /**
     * Android dual-fires D-pad as {@code Keys.UP}/{@code DOWN}/{@code LEFT}/{@code RIGHT} (same
     * ints as {@code DPAD_*}). When a Controllers pad is active, drop those key bits so actions do
     * not double-fire key + pad for the same physical press.
     */
    static boolean shouldSuppressPadEmulatedKey(Key key, boolean padPresent) {
        if (!padPresent || key == null) {
            return false;
        }
        switch (key) {
            case UP:
            case DOWN:
            case LEFT:
            case RIGHT:
                return true;
            default:
                return false;
        }
    }
}
