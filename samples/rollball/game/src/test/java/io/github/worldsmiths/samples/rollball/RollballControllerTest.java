package io.github.worldsmiths.samples.rollball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RollballControllerTest {

    @Test
    void collectingLastPickupWins() {
        RollballController game = new RollballController();
        assertEquals(RollballController.PICKUP_COUNT, game.remainingPickups());
        assertFalse(game.won());

        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            float[] p = game.pickupPosition(i);
            game.placeBall(p[0], RollballController.BALL_RADIUS, p[1]);
            game.update(0.016f, 0f, 0f);
        }

        assertEquals(0, game.remainingPickups());
        assertEquals(RollballController.PICKUP_COUNT, game.score());
        assertTrue(game.won());
        assertFalse(game.failed());
    }

    @Test
    void leavingArenaBoundsFails() {
        RollballController game = new RollballController();
        game.placeBall(RollballController.ARENA_HALF + 0.1f, RollballController.BALL_RADIUS, 0f);
        game.update(0.016f, 0f, 0f);
        assertTrue(game.failed());
        assertFalse(game.won());
    }

    @Test
    void fallingBelowFloorFails() {
        RollballController game = new RollballController();
        // Past the floor pad so gravity applies, then drop below FAIL_Y.
        game.placeBall(RollballController.FLOOR_HALF + 0.5f, RollballController.FALL_Y - 0.1f, 0f);
        game.update(0.016f, 0f, 0f);
        assertTrue(game.failed());
        assertFalse(game.won());
    }

    @Test
    void restartClearsWinAndRestoresPickups() {
        RollballController game = new RollballController();
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            float[] p = game.pickupPosition(i);
            game.placeBall(p[0], RollballController.BALL_RADIUS, p[1]);
            game.update(0.016f, 0f, 0f);
        }
        assertTrue(game.won());

        game.restart();

        assertFalse(game.won());
        assertFalse(game.failed());
        assertEquals(0, game.score());
        assertEquals(RollballController.PICKUP_COUNT, game.remainingPickups());
        assertEquals(0f, game.ballX(), 1e-5f);
        assertEquals(RollballController.BALL_RADIUS, game.ballY(), 1e-5f);
        assertEquals(0f, game.ballZ(), 1e-5f);
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            assertFalse(game.pickupCollected(i));
        }
    }

    @Test
    void moveAxesAccelerateBallOnFloor() {
        RollballController game = new RollballController();
        game.update(0.5f, 1f, 0f);
        assertTrue(game.ballX() > 0.1f);
        assertEquals(RollballController.BALL_RADIUS, game.ballY(), 1e-4f);
        assertFalse(game.failed());
    }

    @Test
    void ignoresMoveWhenFailed() {
        RollballController game = new RollballController();
        game.placeBall(RollballController.ARENA_HALF + 1f, RollballController.BALL_RADIUS, 0f);
        game.update(0.016f, 0f, 0f);
        assertTrue(game.failed());
        float x = game.ballX();
        game.update(0.5f, -1f, 0f);
        assertEquals(x, game.ballX(), 1e-5f);
    }
}
