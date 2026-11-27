package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.PadButton;

/**
 * Soft-absent gamepad used on iOS for ticket 04 (and any host without a Controllers backend). Bind
 * succeeds; every poll is false. Not a hardware AC — Controllers forceLink remains out of scope.
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
