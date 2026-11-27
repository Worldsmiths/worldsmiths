package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.PadButton;

/**
 * Package-private SPI for digital gamepad polls into {@link io.github.worldsmiths.core.DeviceState}.
 * Controllers types stay behind this seam — never part of the author API.
 */
interface GamepadSource {

    /** Whether at least one pad is connected and readable this frame. */
    boolean isPresent();

    /** Digital button hold. Soft-false when no pad / unmapped / undefined mapping code. */
    boolean isButtonDown(PadButton button);
}
