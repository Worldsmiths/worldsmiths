package io.github.worldsmiths.samples.relay;

/**
 * P2 relay stub: move an agent on the XZ ground plane into one active checkpoint (AABB overlap).
 * An optional second gate is visible but inactive. No Bullet / GDX.
 */
public final class RelayController {

    public static final float AGENT_RADIUS = 0.4f;
    public static final float MOVE_SPEED = 5f;
    /** Half-extent of the AABB checkpoint volume on X and Z (Y ignored; ground-plane stub). */
    public static final float CHECKPOINT_HALF = 0.75f;

    private static final float ACTIVE_X = 3f;
    private static final float ACTIVE_Z = 0f;
    private static final float INACTIVE_X = -3f;
    private static final float INACTIVE_Z = 0f;

    private float agentX;
    private float agentY = AGENT_RADIUS;
    private float agentZ;
    private boolean legComplete;

    public float agentX() {
        return agentX;
    }

    public float agentY() {
        return agentY;
    }

    public float agentZ() {
        return agentZ;
    }

    public boolean legComplete() {
        return legComplete;
    }

    public boolean terminal() {
        return legComplete;
    }

    /** Active gate center as {@code {x, z}}. */
    public float[] activeCheckpointCenter() {
        return new float[] {ACTIVE_X, ACTIVE_Z};
    }

    /** Inactive (visible-only) gate center as {@code {x, z}}. */
    public float[] inactiveCheckpointCenter() {
        return new float[] {INACTIVE_X, INACTIVE_Z};
    }

    /** Test / sync seam: teleports the agent. */
    void placeAgent(float x, float y, float z) {
        agentX = x;
        agentY = y;
        agentZ = z;
    }

    /**
     * Advances by {@code delta} seconds using normalized move axes on XZ. Ignores move when the leg
     * is already complete. Overlap uses AABB vs agent center (implementer choice: AABB).
     */
    public void update(float delta, float axisX, float axisZ) {
        if (legComplete || delta <= 0f) {
            return;
        }
        float ax = axisX;
        float az = axisZ;
        float lenSq = ax * ax + az * az;
        if (lenSq > 1f) {
            float inv = (float) (1.0 / Math.sqrt(lenSq));
            ax *= inv;
            az *= inv;
        }
        agentX += ax * MOVE_SPEED * delta;
        agentZ += az * MOVE_SPEED * delta;
        agentY = AGENT_RADIUS;

        if (overlapsAabb(ACTIVE_X, ACTIVE_Z)) {
            legComplete = true;
        }
    }

    /** Restores agent and leg flag to the initial run. */
    public void restart() {
        agentX = 0f;
        agentY = AGENT_RADIUS;
        agentZ = 0f;
        legComplete = false;
    }

    private boolean overlapsAabb(float centerX, float centerZ) {
        return Math.abs(agentX - centerX) <= CHECKPOINT_HALF
                && Math.abs(agentZ - centerZ) <= CHECKPOINT_HALF;
    }
}
