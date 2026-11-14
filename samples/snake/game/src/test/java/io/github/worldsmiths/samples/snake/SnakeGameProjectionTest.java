package io.github.worldsmiths.samples.snake;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.badlogic.gdx.math.Matrix4;
import org.junit.jupiter.api.Test;

class SnakeGameProjectionTest {

    @Test
    void screenOrthoMatrixMatchesPixelSpaceSetToOrtho2D() {
        Matrix4 expected = new Matrix4().setToOrtho2D(0f, 0f, 640f, 480f);

        Matrix4 actual = SnakeGame.screenOrthoMatrix(640f, 480f);

        assertArrayEquals(expected.val, actual.val, 1e-5f);
    }
}
