package io.github.worldsmiths.core.d3;

/**
 * GPU mesh asset. Create instances via {@link D3#createInstance(Model)}. Author-owned: call
 * {@link #dispose()} at shutdown for every model you built.
 */
public interface Model {

    /** Releases backend resources. Safe to call more than once. */
    void dispose();
}
