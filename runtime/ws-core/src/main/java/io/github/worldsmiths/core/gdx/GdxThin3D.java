package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.thin3d.Environment;
import io.github.worldsmiths.core.thin3d.Model;
import io.github.worldsmiths.core.thin3d.ModelBatch;
import io.github.worldsmiths.core.thin3d.ModelBuilder;
import io.github.worldsmiths.core.thin3d.ModelInstance;
import io.github.worldsmiths.core.thin3d.OrthographicCamera;
import io.github.worldsmiths.core.thin3d.PerspectiveCamera;
import io.github.worldsmiths.core.thin3d.Thin3D;

/**
 * GDX-backed {@link Thin3D} factory. Not part of the author-facing API — backends assemble this via
 * {@link GdxEngineServices}. Authors own dispose of {@link Model} / {@link ModelBatch} they create;
 * this factory itself needs no dispose.
 */
final class GdxThin3D implements Thin3D {

    GdxThin3D() {}

    @Override
    public PerspectiveCamera createPerspectiveCamera(
            float fieldOfViewY, float viewportWidth, float viewportHeight) {
        return new GdxPerspectiveCamera(
                new com.badlogic.gdx.graphics.PerspectiveCamera(
                        fieldOfViewY, viewportWidth, viewportHeight));
    }

    @Override
    public OrthographicCamera createOrthographicCamera(float viewportWidth, float viewportHeight) {
        return new GdxOrthographicCamera(
                new com.badlogic.gdx.graphics.OrthographicCamera(viewportWidth, viewportHeight));
    }

    @Override
    public Environment createEnvironment() {
        return new GdxEnvironment(new com.badlogic.gdx.graphics.g3d.Environment());
    }

    @Override
    public ModelBuilder createModelBuilder() {
        return new GdxModelBuilder(new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder());
    }

    @Override
    public ModelBatch createModelBatch() {
        return new GdxModelBatch(new com.badlogic.gdx.graphics.g3d.ModelBatch());
    }

    @Override
    public ModelInstance createInstance(Model model) {
        if (!(model instanceof GdxModel)) {
            throw new IllegalArgumentException("model must come from Thin3D.createModelBuilder()");
        }
        return new GdxModelInstance(
                new com.badlogic.gdx.graphics.g3d.ModelInstance(((GdxModel) model).delegate));
    }
}
