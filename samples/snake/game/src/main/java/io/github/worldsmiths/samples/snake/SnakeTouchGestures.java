package io.github.worldsmiths.samples.snake;

/**
 * Classifies a finger press-release into a swipe or tap using screen coordinates (origin top-left,
 * Y down — libGDX {@code Gdx.input} space).
 */
public final class SnakeTouchGestures {

    /** Minimum travel in pixels before a press-release counts as a swipe. */
    public static final float MIN_SWIPE_PX = 40f;

    public enum Gesture {
        NONE,
        TAP,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private SnakeTouchGestures() {}

    /**
     * @param downX touch-down X
     * @param downY touch-down Y (screen, Y down)
     * @param upX touch-up X
     * @param upY touch-up Y (screen, Y down)
     */
    public static Gesture classify(float downX, float downY, float upX, float upY) {
        float dx = upX - downX;
        float dy = upY - downY;
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);
        if (absDx < MIN_SWIPE_PX && absDy < MIN_SWIPE_PX) {
            return Gesture.TAP;
        }
        if (absDx >= absDy) {
            return dx > 0f ? Gesture.RIGHT : Gesture.LEFT;
        }
        // Screen Y increases downward: finger moved up → negative dy → UP.
        return dy < 0f ? Gesture.UP : Gesture.DOWN;
    }
}
