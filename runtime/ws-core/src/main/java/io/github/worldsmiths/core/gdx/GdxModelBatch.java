package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.d3.Camera;
import io.github.worldsmiths.core.d3.Environment;
import io.github.worldsmiths.core.d3.ModelBatch;
import io.github.worldsmiths.core.d3.ModelInstance;

/** GDX-backed {@link ModelBatch}. Not part of the author-facing API. */
final class GdxModelBatch implements ModelBatch {
    private final com.badlogic.gdx.graphics.g3d.ModelBatch batch;
    private boolean disposed;

    GdxModelBatch(com.badlogic.gdx.graphics.g3d.ModelBatch batch) {
        this.batch = batch;
    }

    @Override
    public void begin(Camera camera) {
        batch.begin(unwrap(camera));
    }

    @Override
    public void render(ModelInstance instance, Environment environment) {
        if (!(instance instanceof GdxModelInstance)) {
            throw new IllegalArgumentException("instance must come from D3.createInstance()");
        }
        if (!(environment instanceof GdxEnvironment)) {
            throw new IllegalArgumentException(
                    "environment must come from D3.createEnvironment()");
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

    private static com.badlogic.gdx.graphics.Camera unwrap(Camera camera) {
        if (camera instanceof GdxPerspectiveCamera) {
            return ((GdxPerspectiveCamera) camera).delegate();
        }
        if (camera instanceof GdxOrthographicCamera) {
            return ((GdxOrthographicCamera) camera).delegate();
        }
        throw new IllegalArgumentException(
                "camera must come from D3.createPerspectiveCamera() or createOrthographicCamera()");
    }
}
