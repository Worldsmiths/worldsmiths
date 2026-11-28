package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import io.github.worldsmiths.core.d3.Ray;
import org.junit.jupiter.api.Test;

/**
 * Bind peer of PickRayGoldenTest: {@link GdxPickRays} matches the documented Input-space ortho
 * center pick within {@code 1e-4f}, without a display or OrthographicCamera (avoids native frustum
 * update).
 */
class GdxPickRaysTest {

    private static final float EPS = 1e-4f;

    @Test
    void fromCameraMatchesOrthoCenterGolden() {
        // Same pose as PickRayGoldenTest: ortho 12×12, eye (0,0,10) → origin, near=1.
        Matrix4 projection = new Matrix4().setToOrtho(-6f, 6f, -6f, 6f, 1f, 100f);
        Matrix4 view =
                new Matrix4()
                        .setToLookAt(new Vector3(0f, 0f, 10f), new Vector3(0f, 0f, 0f), new Vector3(0f, 1f, 0f));
        PerspectiveCamera holder = new PerspectiveCamera();
        holder.invProjectionView.set(projection).mul(view).inv();

        Ray ray = GdxPickRays.fromCamera(holder, 400f, 300f, 800f, 600f);

        assertEquals(0f, ray.direction.x, EPS);
        assertEquals(0f, ray.direction.y, EPS);
        assertEquals(-1f, ray.direction.z, EPS);
        assertEquals(0f, ray.origin.x, EPS);
        assertEquals(0f, ray.origin.y, EPS);
        assertEquals(9f, ray.origin.z, EPS);
    }
}
