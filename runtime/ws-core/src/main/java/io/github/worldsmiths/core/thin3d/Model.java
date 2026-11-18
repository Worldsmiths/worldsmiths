package io.github.worldsmiths.core.thin3d;

/**
 * GPU mesh asset. Create instances via {@link Thin3D#createInstance(Model)}. Author-owned: call
 * {@link #dispose()} at shutdown for every model you built.
 */
public interface Model {

    /** Releases backend resources. Safe to call more than once. */
    void dispose();
}
