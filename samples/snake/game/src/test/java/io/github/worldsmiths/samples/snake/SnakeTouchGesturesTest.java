package io.github.worldsmiths.samples.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SnakeTouchGesturesTest {

    @Test
    void rightSwipePastThresholdIsRight() {
        assertEquals(
                SnakeTouchGestures.Gesture.RIGHT,
                SnakeTouchGestures.classify(100f, 200f, 180f, 205f));
    }

    @Test
    void leftSwipePastThresholdIsLeft() {
        assertEquals(
                SnakeTouchGestures.Gesture.LEFT,
                SnakeTouchGestures.classify(180f, 200f, 100f, 205f));
    }

    @Test
    void upSwipePastThresholdIsUp() {
        // Screen Y grows downward; finger moves up → smaller Y.
        assertEquals(
                SnakeTouchGestures.Gesture.UP,
                SnakeTouchGestures.classify(100f, 250f, 105f, 150f));
    }

    @Test
    void downSwipePastThresholdIsDown() {
        assertEquals(
                SnakeTouchGestures.Gesture.DOWN,
                SnakeTouchGestures.classify(100f, 150f, 105f, 250f));
    }

    @Test
    void shortMovementIsTap() {
        assertEquals(
                SnakeTouchGestures.Gesture.TAP,
                SnakeTouchGestures.classify(100f, 200f, 110f, 208f));
    }
}
