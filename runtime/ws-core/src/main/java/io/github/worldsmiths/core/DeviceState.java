package io.github.worldsmiths.core;

import java.util.EnumSet;

/**
 * Shared held-bit port for keyboard, pointer buttons, and gamepad. Fake setters and (later) GDX
 * resolvers write here; {@link Actions} polls the same bits through {@link Control}s.
 */
public final class DeviceState {
    private final EnumSet<Key> keysDown = EnumSet.noneOf(Key.class);
    private final EnumSet<PadButton> padButtonsDown = EnumSet.noneOf(PadButton.class);
    private final EnumSet<PointerButton> pointerButtonsDown = EnumSet.noneOf(PointerButton.class);

    public boolean isKeyDown(Key key) {
        return key != null && keysDown.contains(key);
    }

    public boolean isPadButtonDown(PadButton button) {
        return button != null && padButtonsDown.contains(button);
    }

    public boolean isPointerButtonDown(PointerButton button) {
        return button != null && pointerButtonsDown.contains(button);
    }

    public void setKeyDown(Key key, boolean down) {
        if (key == null) {
            return;
        }
        if (down) {
            keysDown.add(key);
        } else {
            keysDown.remove(key);
        }
    }

    public void setPadButtonDown(PadButton button, boolean down) {
        if (button == null) {
            return;
        }
        if (down) {
            padButtonsDown.add(button);
        } else {
            padButtonsDown.remove(button);
        }
    }

    public void setPointerButtonDown(PointerButton button, boolean down) {
        if (button == null) {
            return;
        }
        if (down) {
            pointerButtonsDown.add(button);
        } else {
            pointerButtonsDown.remove(button);
        }
    }
}
