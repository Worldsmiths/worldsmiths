package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import io.github.worldsmiths.core.EngineException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fail-closed pick when no live graphics surface. Exercises {@link GdxPickRays#fromGdxCamera}, the
 * shared path used by both GDX camera wrappers (avoids OrthographicCamera ctor natives).
 */
class GdxCameraPickRayTest {

    @BeforeEach
    void clearGraphics() {
        Gdx.graphics = null;
    }

    @AfterEach
    void clearGraphicsAfter() {
        Gdx.graphics = null;
    }

    @Test
    void fromGdxCameraWithoutGraphicsThrowsEngineException() {
        // Bare PerspectiveCamera() skips frustum update / natives (see GdxPickRaysTest).
        PerspectiveCamera holder = new PerspectiveCamera();

        EngineException thrown =
                assertThrows(
                        EngineException.class, () -> GdxPickRays.fromGdxCamera(holder, 10f, 20f));
        assertTrue(thrown.getMessage().contains("graphics"));
    }

    @Test
    void perspectiveWrapperGetPickRayWithoutGraphicsThrowsEngineException() {
        GdxPerspectiveCamera camera = new GdxPerspectiveCamera(new PerspectiveCamera());

        EngineException thrown =
                assertThrows(EngineException.class, () -> camera.getPickRay(10f, 20f));
        assertTrue(thrown.getMessage().contains("graphics"));
    }
}
