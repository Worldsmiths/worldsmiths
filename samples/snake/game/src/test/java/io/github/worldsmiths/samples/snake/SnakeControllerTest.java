package io.github.worldsmiths.samples.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void wrapsAtGridEdges() {
        SnakeController snake = new SnakeController(19, 5, 20, 15);
        snake.update(SnakeController.STEP_SECONDS);
        assertEquals(0, snake.x());
        assertEquals(5, snake.y());
    }
}
