package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.thin3d.ModelInstance;

/** GDX-backed {@link ModelInstance}. Not part of the author-facing API. */
final class GdxModelInstance implements ModelInstance {
    final com.badlogic.gdx.graphics.g3d.ModelInstance delegate;

    GdxModelInstance(com.badlogic.gdx.graphics.g3d.ModelInstance delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setTranslation(float x, float y, float z) {
        delegate.transform.setToTranslation(x, y, z);
    }

    @Override
    public void setTranslationAndScaling(
            float x, float y, float z, float scaleX, float scaleY, float scaleZ) {
        delegate.transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
    }
}
