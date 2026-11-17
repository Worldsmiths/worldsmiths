package io.github.worldsmiths.samples.rollball;

/**
 * Pure touch drag-stick + restart-tap helpers for rollball. Coordinates are screen space with origin
 * top-left, Y down (libGDX {@code Gdx.input} space). Finger-down is the stick origin; drag while held
 * maps to world XZ axes; release zeros axes and may request restart when terminal.
 */
public final class RollballTouchGestures {

    /** Drag distance (px) inside which axes stay zero. */
    public static final float DEAD_ZONE_PX = 12f;

    /** Drag distance (px) that maps to full axis magnitude (±1). */
    public static final float FULL_DEFLECTION_PX = 80f;

    /** Max travel for a press-release to count as a restart tap. */
    public static final float TAP_MAX_PX = 40f;

    private RollballTouchGestures() {}

    /**
     * Continuous drag-stick while the finger is held. Screen right → +X; screen up (negative dy) →
     * −Z (forward). When {@code terminal}, axes are always zero (restart is decided on release).
     */
    public static RollballMoveIntent dragStick(
            float originX, float originY, float currentX, float currentY, boolean terminal) {
        if (terminal) {
            return RollballMoveIntent.of(0f, 0f, false);
        }
        float dx = currentX - originX;
        // Screen Y grows downward; finger moved up → negative dy → forward (−Z).
        float dz = currentY - originY;
        float absDx = Math.abs(dx);
        float absDz = Math.abs(dz);
        if (absDx < DEAD_ZONE_PX && absDz < DEAD_ZONE_PX) {
            return RollballMoveIntent.of(0f, 0f, false);
        }
        float x = dx / FULL_DEFLECTION_PX;
        float z = dz / FULL_DEFLECTION_PX;
        float lenSq = x * x + z * z;
        if (lenSq > 1f) {
            float inv = (float) (1.0 / Math.sqrt(lenSq));
            x *= inv;
            z *= inv;
        }
        return RollballMoveIntent.of(x, z, false);
    }

    /**
     * Finger release: always zero axes. Restart is true only when {@code terminal} and travel was a
     * tap (below {@link #TAP_MAX_PX}).
     */
    public static RollballMoveIntent release(
            float downX, float downY, float upX, float upY, boolean terminal) {
        boolean restart = terminal && isTap(downX, downY, upX, upY);
        return RollballMoveIntent.of(0f, 0f, restart);
    }

    static boolean isTap(float downX, float downY, float upX, float upY) {
        float dx = upX - downX;
        float dy = upY - downY;
        return Math.abs(dx) < TAP_MAX_PX && Math.abs(dy) < TAP_MAX_PX;
    }
}
