package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.PadButton;

/**
 * Soft-absent gamepad (iOS hygiene / no Controllers backend). Bind succeeds; every poll is false.
 */
final class EmptyGamepadSource implements GamepadSource {
    static final EmptyGamepadSource INSTANCE = new EmptyGamepadSource();

    private EmptyGamepadSource() {}

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isButtonDown(PadButton button) {
        return false;
    }
}
