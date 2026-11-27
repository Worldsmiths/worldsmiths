package io.github.worldsmiths.core;

/**
 * Engine-owned digital gamepad buttons for {@link Actions} bindings. Values are independent of any
 * backend; GDX Controllers mapping stays private to the gdx package.
 */
public enum PadButton {
    A,
    B,
    X,
    Y,
    START,
    SELECT,
    L1,
    R1,
    DPAD_UP,
    DPAD_DOWN,
    DPAD_LEFT,
    DPAD_RIGHT
}
