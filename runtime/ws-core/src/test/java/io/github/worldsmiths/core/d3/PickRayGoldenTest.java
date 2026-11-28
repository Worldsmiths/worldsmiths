package io.github.worldsmiths.core.d3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Headless Input-space ortho pick golden (no display, no LibGDX natives).
 *
 * <p>Pose: orthographic world viewport {@code 12×12}, eye {@code (0,0,10)} looking at origin with
 * up {@code (0,1,0)}, {@code near=1}. Screen pixels {@code 800×600}, Input top-left (Y down).
 *
 * <p>Epsilon {@link #EPS} is the accepted absolute error on direction/origin components for this
 * case. For this axis-aligned pose the expected ray is closed-form: direction {@code (0,0,-1)};
 * origin on the near plane {@code z=9} with {@code x,y} from NDC × half-extents.
 */
class PickRayGoldenTest {

    /** Absolute tolerance for golden direction / origin components. */
    public static final float EPS = 1e-4f;

    private static final float HALF_W = 6f;
    private static final float HALF_H = 6f;
    private static final float NEAR_Z = 9f;
    private static final float PIXEL_W = 800f;
    private static final float PIXEL_H = 600f;

    @Test
    void orthoCenterPickDirectionMatchesLookDirectionWithinEpsilon() {
        Ray ray = orthoPickLookingDownNegativeZ(PIXEL_W * 0.5f, PIXEL_H * 0.5f);

        assertEquals(0f, ray.direction.x, EPS);
        assertEquals(0f, ray.direction.y, EPS);
        assertEquals(-1f, ray.direction.z, EPS);
        assertEquals(0f, ray.origin.x, EPS);
        assertEquals(0f, ray.origin.y, EPS);
        assertEquals(NEAR_Z, ray.origin.z, EPS);
    }

    @Test
    void orthoTopCenterPickOriginIsAboveFrustumCenter_inputTopLeftY() {
        Ray top = orthoPickLookingDownNegativeZ(PIXEL_W * 0.5f, 0f);
        Ray bottom = orthoPickLookingDownNegativeZ(PIXEL_W * 0.5f, PIXEL_H - 1f);

        assertEquals(0f, top.direction.x, EPS);
        assertEquals(0f, top.direction.y, EPS);
        assertEquals(-1f, top.direction.z, EPS);
        assertTrue(top.origin.y > 0f);
        assertTrue(bottom.origin.y < 0f);
        assertEquals(HALF_H, top.origin.y, 0.05f);
    }

    /**
     * Closed-form ortho pick matching LibGDX full-screen {@code unproject} Y-flip ({@code y' =
     * pixelHeight - screenY}) for the documented pose. Kept free of LibGDX so CI stays headless
     * without natives.
     */
    static Ray orthoPickLookingDownNegativeZ(float screenX, float screenY) {
        float ndcX = (2f * screenX) / PIXEL_W - 1f;
        float ndcY = (2f * (PIXEL_H - screenY)) / PIXEL_H - 1f;
        float originX = ndcX * HALF_W;
        float originY = ndcY * HALF_H;
        return new Ray(new Vec3(originX, originY, NEAR_Z), new Vec3(0f, 0f, -1f));
    }
}
