package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.thin3d.Ray;
import io.github.worldsmiths.core.thin3d.Vec3;

/**
 * Application-free pick-ray copy from a LibGDX camera. Matches LibGDX {@code Camera.unproject} /
 * {@code getPickRay} Input top-left semantics when {@code pixelHeight} equals the graphics height
 * used by that path (full-screen viewport). Package-private — not author API.
 */
final class GdxPickRays {
    private GdxPickRays() {}

    /**
     * Pick through a live {@link Gdx#graphics} surface. Throws {@link EngineException} when graphics
     * is missing (no pixel size for unproject).
     */
    static Ray fromGdxCamera(
            com.badlogic.gdx.graphics.Camera camera, float screenX, float screenY) {
        if (Gdx.graphics == null) {
            throw new EngineException("Gdx.graphics required for Camera.getPickRay");
        }
        return fromCamera(
                camera, screenX, screenY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Builds an immutable engine {@link Ray} for the given Input-space screen pixel through {@code
     * camera}'s current {@code invProjectionView}.
     *
     * @param pixelWidth screen / graphics width in pixels
     * @param pixelHeight screen / graphics height in pixels
     */
    static Ray fromCamera(
            com.badlogic.gdx.graphics.Camera camera,
            float screenX,
            float screenY,
            float pixelWidth,
            float pixelHeight) {
        Vector3 near = unproject(camera, screenX, screenY, 0f, pixelWidth, pixelHeight);
        Vector3 far = unproject(camera, screenX, screenY, 1f, pixelWidth, pixelHeight);
        float dx = far.x - near.x;
        float dy = far.y - near.y;
        float dz = far.z - near.z;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0f) {
            dx /= len;
            dy /= len;
            dz /= len;
        }
        return new Ray(new Vec3(near.x, near.y, near.z), new Vec3(dx, dy, dz));
    }

    /**
     * Same Y-flip as LibGDX {@code Camera.unproject(…, viewportX, viewportY, viewportWidth,
     * viewportHeight)} when {@code viewportY == 0} and {@code pixelHeight} stands in for {@code
     * Gdx.graphics.getHeight()}.
     */
    private static Vector3 unproject(
            com.badlogic.gdx.graphics.Camera camera,
            float screenX,
            float screenY,
            float screenZ,
            float pixelWidth,
            float pixelHeight) {
        float x = screenX;
        float y = pixelHeight - screenY;
        Vector3 out = new Vector3();
        out.x = (2f * x) / pixelWidth - 1f;
        out.y = (2f * y) / pixelHeight - 1f;
        out.z = 2f * screenZ - 1f;
        out.prj(camera.invProjectionView);
        return out;
    }
}
