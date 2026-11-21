package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.thin3d.OrthographicCamera;
import io.github.worldsmiths.core.thin3d.Ray;

/** GDX-backed {@link OrthographicCamera}. Not part of the author-facing API. */
final class GdxOrthographicCamera implements OrthographicCamera {
    private final com.badlogic.gdx.graphics.OrthographicCamera camera;

    GdxOrthographicCamera(com.badlogic.gdx.graphics.OrthographicCamera camera) {
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
        return GdxPickRays.fromGdxCamera(camera, screenX, screenY);
    }

    com.badlogic.gdx.graphics.OrthographicCamera delegate() {
        return camera;
    }
}
