package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.d3.Model;

/** GDX-backed {@link Model}. Not part of the author-facing API. */
final class GdxModel implements Model {
    final com.badlogic.gdx.graphics.g3d.Model delegate;
    private final boolean disposeDelegate;
    private boolean disposed;

    GdxModel(com.badlogic.gdx.graphics.g3d.Model delegate) {
        this(delegate, true);
    }

    /**
     * @param disposeDelegate when {@code false}, author {@link #dispose()} drops only the wrapper
     *     (shared registry mesh stays alive for subsequent binds).
     */
    GdxModel(com.badlogic.gdx.graphics.g3d.Model delegate, boolean disposeDelegate) {
        this.delegate = delegate;
        this.disposeDelegate = disposeDelegate;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (disposeDelegate) {
            delegate.dispose();
        }
    }
}
