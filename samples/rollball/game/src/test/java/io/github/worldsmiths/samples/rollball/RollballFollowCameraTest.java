package io.github.worldsmiths.samples.rollball;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RollballFollowCameraTest {

    @Test
    void eyeIsBehindAndAboveBallLookingAtBall() {
        float[] eye = new float[3];
        float[] lookAt = new float[3];
        RollballFollowCamera.compute(2f, 0.5f, 3f, eye, lookAt);

        assertArrayEquals(new float[] {2f, 0.5f, 3f}, lookAt, 1e-5f);
        assertEquals(2f, eye[0], 1e-5f);
        assertTrue(eye[1] > 0.5f + 1f);
        assertTrue(eye[2] > 3f);
        assertEquals(
                RollballFollowCamera.HEIGHT,
                eye[1] - 0.5f,
                1e-5f);
        assertEquals(RollballFollowCamera.DISTANCE, eye[2] - 3f, 1e-5f);
    }
}
