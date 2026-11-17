package io.github.worldsmiths.samples.rollball;

/**
 * Custom follow-camera math: eye sits behind (+Z) and above the ball, looking at the ball center.
 * Pure floats — no GL / PerspectiveCamera dependency.
 */
public final class RollballFollowCamera {

    public static final float DISTANCE = 8f;
    public static final float HEIGHT = 5f;

    private RollballFollowCamera() {}

    /**
     * Writes eye and look-at positions for a ball at {@code (ballX, ballY, ballZ)}.
     *
     * @param eye length ≥ 3, written as xyz
     * @param lookAt length ≥ 3, written as xyz
     */
    public static void compute(
            float ballX, float ballY, float ballZ, float[] eye, float[] lookAt) {
        lookAt[0] = ballX;
        lookAt[1] = ballY;
        lookAt[2] = ballZ;
        eye[0] = ballX;
        eye[1] = ballY + HEIGHT;
        eye[2] = ballZ + DISTANCE;
    }
}
