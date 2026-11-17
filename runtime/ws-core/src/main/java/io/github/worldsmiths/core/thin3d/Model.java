package io.github.worldsmiths.core.thin3d;

/** GPU mesh asset. Create instances via {@link Thin3D#createInstance(Model)}; call {@link #dispose()} at shutdown. */
public interface Model {

    /** Releases backend resources. Safe to call more than once. */
    void dispose();
}
