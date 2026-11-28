package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.d3.Environment;
import io.github.worldsmiths.core.d3.Model;
import io.github.worldsmiths.core.d3.ModelBatch;
import io.github.worldsmiths.core.d3.ModelBuilder;
import io.github.worldsmiths.core.d3.ModelInstance;
import io.github.worldsmiths.core.d3.OrthographicCamera;
import io.github.worldsmiths.core.d3.PerspectiveCamera;
import io.github.worldsmiths.core.d3.D3;

/**
 * GDX-backed {@link D3} factory. Not part of the author-facing API — backends assemble this via
 * {@link GdxEngineServices}. Authors own dispose of {@link Model} / {@link ModelBatch} they create;
 * this factory itself needs no dispose.
 */
final class GdxD3 implements D3 {
    private final GdxMeshes meshes;

    GdxD3() {
        this(new GdxMeshes());
    }

    GdxD3(GdxMeshes meshes) {
        this.meshes = meshes;
    }

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
            throw new IllegalArgumentException("model must come from this D3 factory");
        }
        return new GdxModelInstance(
                new com.badlogic.gdx.graphics.g3d.ModelInstance(((GdxModel) model).delegate));
    }

    @Override
    public Model createModelFromLoaded(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new EngineException("Cannot bind mesh: path is null or blank");
        }
        if (!meshes.isMeshLoadedForBind(path)) {
            throw new EngineException("Cannot bind mesh before load COMPLETE: " + path);
        }
        com.badlogic.gdx.graphics.g3d.Model gdx = meshes.gdxModelForBind(path);
        if (gdx == null) {
            throw new EngineException("Cannot bind mesh before load COMPLETE: " + path);
        }
        return new GdxModel(gdx, false);
    }
}