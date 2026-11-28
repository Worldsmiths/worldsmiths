package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.d3.Model;

/** GDX-backed {@link Model}. Not part of the author-facing API. */
final class GdxModel implements Model {
    final com.badlogic.gdx.graphics.g3d.Model delegate;
    private boolean disposed;

    GdxModel(com.badlogic.gdx.graphics.g3d.Model delegate) {
        this.delegate = delegate;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        delegate.dispose();
    }
}
