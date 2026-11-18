package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.thin3d.Environment;
import io.github.worldsmiths.core.thin3d.ModelBatch;
import io.github.worldsmiths.core.thin3d.ModelInstance;
import io.github.worldsmiths.core.thin3d.PerspectiveCamera;

/** GDX-backed {@link ModelBatch}. Not part of the author-facing API. */
final class GdxModelBatch implements ModelBatch {
    private final com.badlogic.gdx.graphics.g3d.ModelBatch batch;
    private boolean disposed;

    GdxModelBatch(com.badlogic.gdx.graphics.g3d.ModelBatch batch) {
        this.batch = batch;
    }

    @Override
    public void begin(PerspectiveCamera camera) {
        if (!(camera instanceof GdxPerspectiveCamera)) {
            throw new IllegalArgumentException(
                    "camera must come from Thin3D.createPerspectiveCamera()");
        }
        batch.begin(((GdxPerspectiveCamera) camera).delegate());
    }

    @Override
    public void render(ModelInstance instance, Environment environment) {
        if (!(instance instanceof GdxModelInstance)) {
            throw new IllegalArgumentException("instance must come from Thin3D.createInstance()");
        }
        if (!(environment instanceof GdxEnvironment)) {
            throw new IllegalArgumentException(
                    "environment must come from Thin3D.createEnvironment()");
        }
        batch.render(
                ((GdxModelInstance) instance).delegate,
                ((GdxEnvironment) environment).delegate());
    }

    @Override
    public void end() {
        batch.end();
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        batch.dispose();
    }
}
