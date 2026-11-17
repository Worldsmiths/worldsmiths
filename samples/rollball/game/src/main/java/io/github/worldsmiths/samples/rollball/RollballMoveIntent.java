package io.github.worldsmiths.samples.rollball;

/**
 * Pure move / restart intent for rollball. Keyboard and touch helpers produce this type so {@link
 * RollballGame} can stay a thin {@code Gdx.input} edge.
 */
public final class RollballMoveIntent {

    private final float axisX;
    private final float axisZ;
    private final boolean restart;

    private RollballMoveIntent(float axisX, float axisZ, boolean restart) {
        this.axisX = axisX;
        this.axisZ = axisZ;
        this.restart = restart;
    }

    /** Factory for composed / touch-produced intents (axes already in world XZ). */
    public static RollballMoveIntent of(float axisX, float axisZ, boolean restart) {
        return new RollballMoveIntent(axisX, axisZ, restart);
    }

    /**
     * Merges keyboard and touch intents. Restart if either requests it (axes zeroed). Otherwise sums
     * axes and clamps to the unit circle.
     */
    public static RollballMoveIntent compose(RollballMoveIntent a, RollballMoveIntent b) {
        if (a.restart || b.restart) {
            return new RollballMoveIntent(0f, 0f, true);
        }
        float x = a.axisX + b.axisX;
        float z = a.axisZ + b.axisZ;
        float lenSq = x * x + z * z;
        if (lenSq > 1f) {
            float inv = (float) (1.0 / Math.sqrt(lenSq));
            x *= inv;
            z *= inv;
        }
        return new RollballMoveIntent(x, z, false);
    }

    public float axisX() {
        return axisX;
    }

    public float axisZ() {
        return axisZ;
    }

    public boolean restart() {
        return restart;
    }

    /**
     * Builds intent from digital keys. Axes are world XZ (right +, forward/up key → −Z). When {@code
     * terminal} (won/failed), move axes are zeroed and {@code restartKey} is honored; otherwise
     * restart is always false.
     *
     * @param left A / Left
     * @param right D / Right
     * @param up W / Up (forward, −Z)
     * @param down S / Down (back, +Z)
     * @param restartKey R / Space
     * @param terminal won or failed
     */
    public static RollballMoveIntent fromKeys(
            boolean left,
            boolean right,
            boolean up,
            boolean down,
            boolean restartKey,
            boolean terminal) {
        if (terminal) {
            return new RollballMoveIntent(0f, 0f, restartKey);
        }
        float x = 0f;
        float z = 0f;
        if (left) {
            x -= 1f;
        }
        if (right) {
            x += 1f;
        }
        if (up) {
            z -= 1f;
        }
        if (down) {
            z += 1f;
        }
        float lenSq = x * x + z * z;
        if (lenSq > 1f) {
            float inv = (float) (1.0 / Math.sqrt(lenSq));
            x *= inv;
            z *= inv;
        }
        return new RollballMoveIntent(x, z, false);
    }
}
