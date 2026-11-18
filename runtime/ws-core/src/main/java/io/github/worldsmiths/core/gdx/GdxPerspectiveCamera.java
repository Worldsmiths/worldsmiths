package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.thin3d.PerspectiveCamera;

/** GDX-backed {@link PerspectiveCamera}. Not part of the author-facing API. */
final class GdxPerspectiveCamera implements PerspectiveCamera {
    private final com.badlogic.gdx.graphics.PerspectiveCamera camera;

    GdxPerspectiveCamera(com.badlogic.gdx.graphics.PerspectiveCamera camera) {
        this.camera = camera;
    }

    @Override
    public void setNear(float near) {
        camera.near = near;
    }

    @Override
    public void setFar(float far) {
        camera.far = far;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        camera.position.set(x, y, z);
    }

    @Override
    public void setUp(float x, float y, float z) {
        camera.up.set(x, y, z);
    }

    @Override
    public void lookAt(float x, float y, float z) {
        camera.lookAt(x, y, z);
    }

    @Override
    public void update() {
        camera.update();
    }

    com.badlogic.gdx.graphics.PerspectiveCamera delegate() {
        return camera;
    }
}
