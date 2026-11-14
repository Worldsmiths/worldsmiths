package io.github.worldsmiths.samples.snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Discrete grid movement for the snake: accumulates delta until a step fires, then advances one
 * cell. Turns via {@link #turn(int, int)}; immediate 180° reverse is ignored. Stepping out of
 * bounds or into the body ends the run. Body segments are ordered head-first. Food spawns on a
 * random empty cell; eating grows by one and increments score. {@link #restart()} restores the
 * initial run state.
 */
public final class SnakeController {

    public static final float STEP_SECONDS = 0.15f;

    private final int startX;
    private final int startY;
    private final int gridWidth;
    private final int gridHeight;
    private final Random random;
    private final List<int[]> body = new ArrayList<>();
    private int foodX;
    private int foodY;
    private int score;
    private int dx = 1;
    private int dy = 0;
    private float accumulator;
    private boolean alive = true;

    public SnakeController(int x, int y, int gridWidth, int gridHeight) {
        this(x, y, gridWidth, gridHeight, new Random());
    }

    public SnakeController(int x, int y, int gridWidth, int gridHeight, Random random) {
        this.startX = x;
        this.startY = y;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.random = random;
        body.add(new int[] {x, y});
        placeFood();
    }

    public int x() {
        return body.get(0)[0];
    }

    public int y() {
        return body.get(0)[1];
    }

    public int bodyLength() {
        return body.size();
    }

    public int bodyX(int index) {
        return body.get(index)[0];
    }

    public int bodyY(int index) {
        return body.get(index)[1];
    }

    public int foodX() {
        return foodX;
    }

    public int foodY() {
        return foodY;
    }

    public int score() {
        return score;
    }

    public boolean alive() {
        return alive;
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
        if (!alive) {
            return;
        }
        accumulator += delta;
        while (accumulator >= STEP_SECONDS) {
            accumulator -= STEP_SECONDS;
            step();
            if (!alive) {
                return;
            }
        }
    }

    /** Resets head, body, food, score, and direction to the initial run state. */
    public void restart() {
        body.clear();
        body.add(new int[] {startX, startY});
        dx = 1;
        dy = 0;
        score = 0;
        accumulator = 0f;
        alive = true;
        placeFood();
    }

    private void step() {
        int nextX = x() + dx;
        int nextY = y() + dy;
        if (nextX < 0 || nextX >= gridWidth || nextY < 0 || nextY >= gridHeight) {
            alive = false;
            return;
        }
        boolean grow = nextX == foodX && nextY == foodY;
        if (hitsBody(nextX, nextY, grow)) {
            alive = false;
            return;
        }
        int[] previous = body.get(0);
        body.set(0, new int[] {nextX, nextY});
        for (int i = 1; i < body.size(); i++) {
            int[] current = body.get(i);
            body.set(i, previous);
            previous = current;
        }
        if (grow) {
            body.add(previous);
            score++;
            placeFood();
        }
    }

    private boolean hitsBody(int nextX, int nextY, boolean grow) {
        int last = body.size() - 1;
        for (int i = 0; i < body.size(); i++) {
            if (!grow && i == last) {
                continue;
            }
            int[] segment = body.get(i);
            if (segment[0] == nextX && segment[1] == nextY) {
                return true;
            }
        }
        return false;
    }

    private void placeFood() {
        int gx;
        int gy;
        do {
            gx = random.nextInt(gridWidth);
            gy = random.nextInt(gridHeight);
        } while (occupied(gx, gy));
        foodX = gx;
        foodY = gy;
    }

    private boolean occupied(int gx, int gy) {
        for (int[] segment : body) {
            if (segment[0] == gx && segment[1] == gy) {
                return true;
            }
        }
        return false;
    }
}
