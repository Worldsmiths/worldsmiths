package io.github.worldsmiths.samples.rollball;

/**
 * Continuous roll-a-ball logic on the XZ plane with Y for gravity/fall. Collect all pickups to win;
 * leave arena bounds or fall below the floor to fail. {@link #restart()} restores the initial run.
 * Float sphere collision only — no Bullet / GDX.
 */
public final class RollballController {

    public static final int PICKUP_COUNT = 8;
    public static final float BALL_RADIUS = 0.5f;
    public static final float PICKUP_RADIUS = 0.35f;
    /** Half-extent of the playable floor pad on X and Z. */
    public static final float FLOOR_HALF = 5f;
    /** Hard fail if |x| or |z| exceeds this (beyond the floor). */
    public static final float ARENA_HALF = 7f;
    /** Fail when ball center drops below this Y. */
    public static final float FALL_Y = -3f;
    public static final float GRAVITY = -18f;
    public static final float MOVE_ACCEL = 14f;
    public static final float MAX_SPEED = 8f;
    public static final float FRICTION = 6f;

    private static final float[][] DEFAULT_PICKUPS = {
        {2.5f, 2.5f},
        {-2.5f, 2.5f},
        {2.5f, -2.5f},
        {-2.5f, -2.5f},
        {4f, 0f},
        {-4f, 0f},
        {0f, 4f},
        {0f, -4f},
    };

    private final float[] pickupX = new float[PICKUP_COUNT];
    private final float[] pickupZ = new float[PICKUP_COUNT];
    private final boolean[] collected = new boolean[PICKUP_COUNT];

    private float ballX;
    private float ballY = BALL_RADIUS;
    private float ballZ;
    private float velX;
    private float velY;
    private float velZ;
    private int score;
    private boolean won;
    private boolean failed;

    public RollballController() {
        placeDefaultPickups();
    }

    public float ballX() {
        return ballX;
    }

    public float ballY() {
        return ballY;
    }

    public float ballZ() {
        return ballZ;
    }

    public int score() {
        return score;
    }

    public int remainingPickups() {
        return PICKUP_COUNT - score;
    }

    public boolean won() {
        return won;
    }

    public boolean failed() {
        return failed;
    }

    public boolean terminal() {
        return won || failed;
    }

    /** Returns pickup XZ as {@code {x, z}}. Package-visible for tests and scene sync. */
    public float[] pickupPosition(int index) {
        return new float[] {pickupX[index], pickupZ[index]};
    }

    public boolean pickupCollected(int index) {
        return collected[index];
    }

    /** Test / sync seam: teleports the ball without changing velocity. */
    void placeBall(float x, float y, float z) {
        ballX = x;
        ballY = y;
        ballZ = z;
    }

    /**
     * Advances simulation by {@code delta} seconds using normalized move axes on XZ ({@code axisX},
     * {@code axisZ} in roughly [-1, 1]). Ignores move when won or failed.
     */
    public void update(float delta, float axisX, float axisZ) {
        if (terminal() || delta <= 0f) {
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

        boolean grounded = isGrounded();
        if (grounded) {
            velX += ax * MOVE_ACCEL * delta;
            velZ += az * MOVE_ACCEL * delta;
            float speed = (float) Math.sqrt(velX * velX + velZ * velZ);
            if (speed > MAX_SPEED) {
                float scale = MAX_SPEED / speed;
                velX *= scale;
                velZ *= scale;
            }
            float damp = Math.max(0f, 1f - FRICTION * delta);
            if (Math.abs(ax) < 1e-4f && Math.abs(az) < 1e-4f) {
                velX *= damp;
                velZ *= damp;
            }
            velY = 0f;
            ballY = BALL_RADIUS;
        } else {
            velY += GRAVITY * delta;
        }

        ballX += velX * delta;
        ballY += velY * delta;
        ballZ += velZ * delta;

        if (isGrounded()) {
            ballY = BALL_RADIUS;
            velY = 0f;
        }

        collectOverlappingPickups();
        checkFailConditions();
        if (score >= PICKUP_COUNT) {
            won = true;
        }
    }

    /** Restores ball, pickups, score, and run flags to the initial state. */
    public void restart() {
        ballX = 0f;
        ballY = BALL_RADIUS;
        ballZ = 0f;
        velX = 0f;
        velY = 0f;
        velZ = 0f;
        score = 0;
        won = false;
        failed = false;
        placeDefaultPickups();
    }

    private void placeDefaultPickups() {
        for (int i = 0; i < PICKUP_COUNT; i++) {
            pickupX[i] = DEFAULT_PICKUPS[i][0];
            pickupZ[i] = DEFAULT_PICKUPS[i][1];
            collected[i] = false;
        }
    }

    private boolean isGrounded() {
        return Math.abs(ballX) <= FLOOR_HALF
                && Math.abs(ballZ) <= FLOOR_HALF
                && ballY <= BALL_RADIUS + 0.05f;
    }

    private void collectOverlappingPickups() {
        float reach = BALL_RADIUS + PICKUP_RADIUS;
        float reachSq = reach * reach;
        for (int i = 0; i < PICKUP_COUNT; i++) {
            if (collected[i]) {
                continue;
            }
            float dx = ballX - pickupX[i];
            float dz = ballZ - pickupZ[i];
            if (dx * dx + dz * dz <= reachSq) {
                collected[i] = true;
                score++;
            }
        }
    }

    private void checkFailConditions() {
        if (Math.abs(ballX) > ARENA_HALF || Math.abs(ballZ) > ARENA_HALF || ballY < FALL_Y) {
            failed = true;
        }
    }
}
