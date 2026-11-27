package io.github.worldsmiths.core;

/**
 * Engine-owned pointer buttons for {@link Actions} bindings. Independent of raw {@link Input}
 * touch/coords; GDX maps these onto platform pointer buttons privately.
 */
public enum PointerButton {
    PRIMARY,
    SECONDARY,
    MIDDLE
}
