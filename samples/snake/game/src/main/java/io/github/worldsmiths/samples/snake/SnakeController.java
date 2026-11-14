package io.github.worldsmiths.samples.snake;

/**
 * Discrete grid movement for the snake head: accumulates delta until a step fires, then advances
 * one cell. Turns via {@link #turn(int, int)}; immediate 180° reverse is ignored.
 */
public final class SnakeController {

    public static final float STEP_SECONDS = 0.15f;

    private final int gridWidth;
    private final int gridHeight;
    private int x;
    private int y;
    private int dx = 1;
    private int dy = 0;
    private float accumulator;

    public SnakeController(int x, int y, int gridWidth, int gridHeight) {
        this.x = x;
        this.y = y;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /**
     * Queues a direction change for the next step. Opposite of the current direction is ignored.
     */
    public void turn(int newDx, int newDy) {
        if (newDx == -dx && newDy == -dy) {
            return;
        }
        if (newDx == 0 && newDy == 0) {
            return;
        }
        dx = newDx;
        dy = newDy;
    }

    public void update(float delta) {
        accumulator += delta;
        while (accumulator >= STEP_SECONDS) {
            accumulator -= STEP_SECONDS;
            x = wrap(x + dx, gridWidth);
            y = wrap(y + dy, gridHeight);
        }
    }

    private static int wrap(int value, int size) {
        int mod = value % size;
        return mod < 0 ? mod + size : mod;
    }
}
