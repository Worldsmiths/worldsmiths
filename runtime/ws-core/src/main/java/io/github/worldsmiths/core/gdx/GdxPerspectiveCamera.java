package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.thin3d.PerspectiveCamera;
import io.github.worldsmiths.core.thin3d.Ray;

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

    @Override
    public Ray getPickRay(float screenX, float screenY) {
        if (Gdx.graphics == null) {
            throw new IllegalStateException("Gdx.graphics required for PerspectiveCamera.getPickRay");
        }
        return GdxPickRays.fromCamera(
                camera, screenX, screenY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    com.badlogic.gdx.graphics.PerspectiveCamera delegate() {
        return camera;
    }
}
