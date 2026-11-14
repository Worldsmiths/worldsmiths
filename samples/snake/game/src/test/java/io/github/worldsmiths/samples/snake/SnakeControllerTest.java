package io.github.worldsmiths.samples.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class SnakeControllerTest {

    @Test
    void advancesOneCellAlongCurrentDirectionAfterStepInterval() {
        SnakeController snake = new SnakeController(5, 5, 20, 15);
        snake.update(SnakeController.STEP_SECONDS);
        assertEquals(6, snake.x());
        assertEquals(5, snake.y());
    }

    @Test
    void doesNotMoveBeforeStepIntervalElapses() {
        SnakeController snake = new SnakeController(5, 5, 20, 15);
        snake.update(SnakeController.STEP_SECONDS * 0.5f);
        assertEquals(5, snake.x());
        assertEquals(5, snake.y());
    }

    @Test
    void turnChangesDirectionForNextStep() {
        SnakeController snake = new SnakeController(5, 5, 20, 15);
        snake.turn(0, 1);
        snake.update(SnakeController.STEP_SECONDS);
        assertEquals(5, snake.x());
        assertEquals(6, snake.y());
    }

    @Test
    void rejectsImmediateReversal() {
        SnakeController snake = new SnakeController(5, 5, 20, 15);
        snake.turn(-1, 0);
        snake.update(SnakeController.STEP_SECONDS);
        assertEquals(6, snake.x());
        assertEquals(5, snake.y());
    }

    @Test
    void diesWhenSteppingOutOfBounds() {
        SnakeController snake = new SnakeController(19, 5, 20, 15);
        snake.update(SnakeController.STEP_SECONDS);
        assertFalse(snake.alive());
        assertEquals(19, snake.x());
        assertEquals(5, snake.y());
    }

    @Test
    void bodyStartsAsSingleHeadSegment() {
        SnakeController snake = new SnakeController(5, 5, 20, 15);
        assertEquals(1, snake.bodyLength());
        assertEquals(5, snake.bodyX(0));
        assertEquals(5, snake.bodyY(0));
    }

    @Test
    void placesFoodOnEmptyCellUsingInjectedRandom() {
        SnakeController snake = new SnakeController(0, 0, 2, 2, scriptedCoords(1, 0));
        assertEquals(1, snake.foodX());
        assertEquals(0, snake.foodY());
    }

    @Test
    void foodPlacementSkipsOccupiedCells() {
        SnakeController snake = new SnakeController(0, 0, 2, 2, scriptedCoords(0, 0, 1, 0));
        assertEquals(1, snake.foodX());
        assertEquals(0, snake.foodY());
    }

    @Test
    void eatingFoodGrowsBodyAndIncrementsScore() {
        SnakeController snake = new SnakeController(0, 0, 3, 1, scriptedCoords(1, 0, 2, 0));
        assertEquals(1, snake.foodX());
        assertEquals(0, snake.foodY());
        assertEquals(0, snake.score());

        snake.update(SnakeController.STEP_SECONDS);

        assertEquals(2, snake.bodyLength());
        assertEquals(1, snake.x());
        assertEquals(0, snake.y());
        assertEquals(0, snake.bodyX(1));
        assertEquals(0, snake.bodyY(1));
        assertEquals(1, snake.score());
        assertEquals(2, snake.foodX());
        assertEquals(0, snake.foodY());
        assertTrue(snake.alive());
    }

    @Test
    void bodySegmentsFollowHeadAfterStepWithoutGrowing() {
        SnakeController snake = new SnakeController(0, 0, 4, 2, scriptedCoords(1, 0, 3, 0));
        snake.update(SnakeController.STEP_SECONDS);
        assertEquals(2, snake.bodyLength());
        assertEquals(1, snake.score());
        assertEquals(3, snake.foodX());
        assertEquals(0, snake.foodY());

        snake.update(SnakeController.STEP_SECONDS);

        assertEquals(2, snake.bodyLength());
        assertEquals(2, snake.x());
        assertEquals(0, snake.y());
        assertEquals(1, snake.bodyX(1));
        assertEquals(0, snake.bodyY(1));
        assertEquals(1, snake.score());
    }

    @Test
    void diesOnSelfCollision() {
        // Grow into a square then turn into a non-tail body cell.
        SnakeController snake =
                new SnakeController(2, 2, 8, 8, scriptedCoords(3, 2, 4, 2, 4, 3, 3, 3, 0, 0));
        snake.update(SnakeController.STEP_SECONDS); // eat (3,2)
        snake.update(SnakeController.STEP_SECONDS); // eat (4,2)
        snake.turn(0, 1);
        snake.update(SnakeController.STEP_SECONDS); // eat (4,3)
        snake.turn(-1, 0);
        snake.update(SnakeController.STEP_SECONDS); // eat (3,3); body length 5
        snake.turn(0, -1);
        snake.update(SnakeController.STEP_SECONDS); // into (3,2) — still occupied

        assertFalse(snake.alive());
        assertEquals(3, snake.x());
        assertEquals(3, snake.y());
    }

    @Test
    void doesNotMoveWhileDead() {
        SnakeController snake = new SnakeController(19, 5, 20, 15, scriptedCoords(0, 0));
        snake.update(SnakeController.STEP_SECONDS);
        assertFalse(snake.alive());

        snake.turn(0, 1);
        snake.update(SnakeController.STEP_SECONDS);

        assertEquals(19, snake.x());
        assertEquals(5, snake.y());
        assertFalse(snake.alive());
    }

    @Test
    void restartRestoresInitialState() {
        SnakeController snake = new SnakeController(0, 0, 4, 2, scriptedCoords(1, 0, 3, 1, 1, 0));
        snake.update(SnakeController.STEP_SECONDS);
        assertEquals(1, snake.score());
        assertEquals(2, snake.bodyLength());
        snake.update(SnakeController.STEP_SECONDS);
        snake.update(SnakeController.STEP_SECONDS);
        snake.update(SnakeController.STEP_SECONDS);
        assertFalse(snake.alive());

        snake.restart();

        assertTrue(snake.alive());
        assertEquals(0, snake.x());
        assertEquals(0, snake.y());
        assertEquals(1, snake.bodyLength());
        assertEquals(0, snake.score());
        assertEquals(1, snake.foodX());
        assertEquals(0, snake.foodY());
        snake.turn(0, 1);
        snake.update(SnakeController.STEP_SECONDS);
        assertEquals(0, snake.x());
        assertEquals(1, snake.y());
        assertTrue(snake.alive());
    }

    /** Returns x,y pairs in order for {@code nextInt} calls used by food placement. */
    private static Random scriptedCoords(int... coords) {
        return new Random() {
            private int index;

            @Override
            public int nextInt(int bound) {
                return coords[index++];
            }
        };
    }
}
