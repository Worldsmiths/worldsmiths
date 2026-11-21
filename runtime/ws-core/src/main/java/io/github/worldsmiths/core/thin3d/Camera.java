package io.github.worldsmiths.core.thin3d;

/**
 * Shared Thin3D camera: pose, {@link #update}, and Input-space pick-ray. Screen coords for {@link
 * #getPickRay} match {@link io.github.worldsmiths.core.Input} (origin top-left, Y down). Call {@link
 * #update} after pose changes before picking or {@link ModelBatch#begin}. No dispose.
 */
public interface Camera {

    void setNear(float near);

    void setFar(float far);

    void setPosition(float x, float y, float z);

    void setUp(float x, float y, float z);

    void lookAt(float x, float y, float z);

    /** Recomputes the view/projection matrices after pose changes. */
    void update();

    /**
     * Pick ray through the given screen pixel in Input space (top-left origin, Y down). Returns a
     * fresh immutable {@link Ray}; never a reused buffer.
     */
    Ray getPickRay(float screenX, float screenY);
}
