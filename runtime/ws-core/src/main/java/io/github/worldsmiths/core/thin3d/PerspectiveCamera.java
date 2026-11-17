package io.github.worldsmiths.core.thin3d;

/**
 * Engine-owned perspective camera. Typical frame: set pose → {@link #lookAt} → {@link #update}, then
 * pass to {@link ModelBatch#begin(PerspectiveCamera)}.
 */
public interface PerspectiveCamera {

    void setNear(float near);

    void setFar(float far);

    void setPosition(float x, float y, float z);

    void setUp(float x, float y, float z);

    void lookAt(float x, float y, float z);

    /** Recomputes the view/projection matrices after pose changes. */
    void update();
}
