package io.github.worldsmiths.core;

/**
 * Opaque digital binding target created via {@link Controls}. Authors never construct subtypes;
 * {@link Actions} evaluates whether the control is currently down against {@link DeviceState}.
 */
public abstract class Control {
    Control() {}

    abstract boolean isDown(DeviceState devices);
}
