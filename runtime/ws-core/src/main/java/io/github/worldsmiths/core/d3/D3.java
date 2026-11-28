package io.github.worldsmiths.core.d3;

/**
 * Factory for the D3 author surface (camera, lights/environment, primitive meshes, batch). No
 * LibGDX types in the contract. Obtain via {@link io.github.worldsmiths.core.EngineServices#d3()}.
 *
 * <p><b>Ownership:</b> {@code D3} itself is not disposable. Authors own {@link Model} and {@link
 * ModelBatch} instances they create and must {@code dispose()} them at shutdown. {@link Camera}
 * subtypes, {@link Environment}, {@link ModelBuilder}, and {@link ModelInstance} need no dispose.
 */
public interface D3 {

    /** Perspective camera with the given vertical FOV (degrees) and viewport size in pixels. */
    PerspectiveCamera createPerspectiveCamera(
            float fieldOfViewY, float viewportWidth, float viewportHeight);

    /**
     * Orthographic camera with the given world-unit viewport size. Pose-only (no zoom / setToOrtho).
     */
    OrthographicCamera createOrthographicCamera(float viewportWidth, float viewportHeight);

    /** Empty lighting environment; add ambient / directional lights before rendering. */
    Environment createEnvironment();

    /** Builder for in-code primitive meshes (sphere/box). No model-file load in D9. */
    ModelBuilder createModelBuilder();

    /** GPU model batch for {@code begin → render → end}. Call {@link ModelBatch#dispose()} at shutdown. */
    ModelBatch createModelBatch();

    /** Drawable instance of a {@link Model}. */
    ModelInstance createInstance(Model model);
}
