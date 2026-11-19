package io.github.worldsmiths.samples.relay;

/**
 * P2 relay: ordered two-leg race on the XZ ground plane (+X then −X AABB checkpoints). Hard run-par
 * FAIL vs injected elapsed. No Bullet / GDX.
 */
public final class RelayController {

    public enum TerminalKind {
        RUNNING,
        CLEAR,
        FAIL
    }

    public static final float AGENT_RADIUS = 0.4f;
    public static final float MOVE_SPEED = 5f;
    /** Half-extent of the AABB checkpoint volume on X and Z (Y ignored; ground-plane stub). */
    public static final float CHECKPOINT_HALF = 0.75f;
    /** Hard run-par seconds; elapsed ≥ this before CLEAR → FAIL. Beatable and failable. */
    public static final float RUN_PAR_SECONDS = 8f;

    private static final float LEG1_X = 3f;
    private static final float LEG1_Z = 0f;
    private static final float LEG2_X = -3f;
    private static final float LEG2_Z = 0f;
    private static final int TOTAL_LEGS = 2;

    private float agentX;
    private float agentY = AGENT_RADIUS;
    private float agentZ;
    private int currentLeg = 1;
    private TerminalKind terminalKind = TerminalKind.RUNNING;

    public float agentX() {
        return agentX;
    }

    public float agentY() {
        return agentY;
    }

    public float agentZ() {
        return agentZ;
    }

    /** 1-based current target leg (1 or 2 while running). */
    public int currentLeg() {
        return currentLeg;
    }

    public int totalLegs() {
        return TOTAL_LEGS;
    }

    public TerminalKind terminalKind() {
        return terminalKind;
    }

    /** True only for course CLEAR (final gate under par). */
    public boolean cleared() {
        return terminalKind == TerminalKind.CLEAR;
    }

    /** True only for hard-par FAIL. */
    public boolean failed() {
        return terminalKind == TerminalKind.FAIL;
    }

    public boolean terminal() {
        return terminalKind != TerminalKind.RUNNING;
    }

    /** Active (current target) gate center as {@code {x, z}}. */
    public float[] activeCheckpointCenter() {
        if (currentLeg >= 2) {
            return new float[] {LEG2_X, LEG2_Z};
        }
        return new float[] {LEG1_X, LEG1_Z};
    }

    /** Other (non-active) gate center as {@code {x, z}}. */
    public float[] inactiveCheckpointCenter() {
        if (currentLeg >= 2) {
            return new float[] {LEG1_X, LEG1_Z};
        }
        return new float[] {LEG2_X, LEG2_Z};
    }

    /** Test / sync seam: teleports the agent. */
    void placeAgent(float x, float y, float z) {
        agentX = x;
        agentY = y;
        agentZ = z;
    }

    /**
     * Advances by {@code delta} seconds using normalized move axes on XZ. {@code runElapsed} is the
     * injected run clock for hard-par FAIL. Ignores move when already terminal. Overlap uses AABB vs
     * agent center.
     */
    public void update(float delta, float axisX, float axisZ, float runElapsed) {
        if (terminalKind != TerminalKind.RUNNING) {
            return;
        }
        if (runElapsed >= RUN_PAR_SECONDS) {
            terminalKind = TerminalKind.FAIL;
            return;
        }
        if (delta > 0f) {
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
        }

        float[] active = activeCheckpointCenter();
        if (overlapsAabb(active[0], active[1])) {
            if (currentLeg == 1) {
                currentLeg = 2;
            } else {
                terminalKind = TerminalKind.CLEAR;
            }
        }
    }

    /** Restores agent, leg index, and terminal kind to the initial run. */
    public void restart() {
        agentX = 0f;
        agentY = AGENT_RADIUS;
        agentZ = 0f;
        currentLeg = 1;
        terminalKind = TerminalKind.RUNNING;
    }

    private boolean overlapsAabb(float centerX, float centerZ) {
        return Math.abs(agentX - centerX) <= CHECKPOINT_HALF
                && Math.abs(agentZ - centerZ) <= CHECKPOINT_HALF;
    }
}
