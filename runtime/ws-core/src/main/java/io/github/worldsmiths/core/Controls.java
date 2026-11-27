package io.github.worldsmiths.core;

/**
 * Factories for typed {@link Control}s used with {@link Actions#bind(String, Control...)}.
 */
public final class Controls {
    private Controls() {}

    public static Control key(Key key) {
        if (key == null) {
            throw new EngineException("Control key must not be null");
        }
        return new KeyControl(key);
    }

    public static Control gamepadButton(PadButton button) {
        if (button == null) {
            throw new EngineException("Control gamepad button must not be null");
        }
        return new GamepadButtonControl(button);
    }

    public static Control pointerButton(PointerButton button) {
        if (button == null) {
            throw new EngineException("Control pointer button must not be null");
        }
        return new PointerButtonControl(button);
    }

    /**
     * All listed keys must be held (chord). Used for seeded {@code ui_focus_prev} (Shift+TAB).
     */
    static Control keys(Key... keys) {
        if (keys == null || keys.length == 0) {
            throw new EngineException("Control keys must not be null or empty");
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null) {
                throw new EngineException("Control key must not be null");
            }
        }
        return new KeyChordControl(keys.clone());
    }

    private static final class KeyControl extends Control {
        private final Key key;

        KeyControl(Key key) {
            this.key = key;
        }

        @Override
        boolean isDown(DeviceState devices) {
            return devices.isKeyDown(key);
        }
    }

    private static final class KeyChordControl extends Control {
        private final Key[] keys;

        KeyChordControl(Key[] keys) {
            this.keys = keys;
        }

        @Override
        boolean isDown(DeviceState devices) {
            for (int i = 0; i < keys.length; i++) {
                if (!devices.isKeyDown(keys[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class GamepadButtonControl extends Control {
        private final PadButton button;

        GamepadButtonControl(PadButton button) {
            this.button = button;
        }

        @Override
        boolean isDown(DeviceState devices) {
            return devices.isPadButtonDown(button);
        }
    }

    private static final class PointerButtonControl extends Control {
        private final PointerButton button;

        PointerButtonControl(PointerButton button) {
            this.button = button;
        }

        @Override
        boolean isDown(DeviceState devices) {
            return devices.isPointerButtonDown(button);
        }
    }
}
