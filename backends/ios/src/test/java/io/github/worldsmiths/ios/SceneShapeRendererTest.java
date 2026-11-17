package io.github.worldsmiths.ios;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.RectDraw;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneWalk;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SceneShapeRendererTest {

    @Test
    void screenOrthoMatrixMatchesPixelSpaceSetToOrtho2D() {
        Matrix4 expected = new Matrix4().setToOrtho2D(0f, 0f, 640f, 480f);

        Matrix4 actual = SceneShapeRenderer.screenOrthoMatrix(640f, 480f);

        assertArrayEquals(expected.val, actual.val, 1e-5f);
    }

    @Test
    void prepareScreenProjectionRecordsDimensionsForDrawPathSeam() {
        SceneShapeRenderer renderer = SceneShapeRenderer.recordingOnly();

        Matrix4 matrix = renderer.prepareScreenProjection(800f, 600f);

        assertEquals(800f, renderer.lastProjectionWidth(), 0.001f);
        assertEquals(600f, renderer.lastProjectionHeight(), 0.001f);
        assertArrayEquals(new Matrix4().setToOrtho2D(0f, 0f, 800f, 600f).val, matrix.val, 1e-5f);
    }

    @Test
    void recordingOnlyRenderDoesNotTouchScreenSizeSuppliers() {
        AtomicInteger widthReads = new AtomicInteger();
        AtomicInteger heightReads = new AtomicInteger();
        SceneShapeRenderer renderer =
                new SceneShapeRenderer(
                        null,
                        new java.util.function.IntSupplier() {
                            @Override
                            public int getAsInt() {
                                widthReads.incrementAndGet();
                                return 100;
                            }
                        },
                        new java.util.function.IntSupplier() {
                            @Override
                            public int getAsInt() {
                                heightReads.incrementAndGet();
                                return 100;
                            }
                        });

        Sprite sprite = new Sprite("x.png");
        Scene scene = new Scene("test").add(new Node("n").add(new Transform2D(1f, 1f)).add(sprite));
        renderer.render(scene);

        assertEquals(0, widthReads.get());
        assertEquals(0, heightReads.get());
        assertEquals(-1f, renderer.lastProjectionWidth(), 0.001f);
        assertNull(renderer.lastProjectionMatrix());
    }

    @Test
    void renderMapsCoreWalkRectsIntoLastDrawCommands() {
        Sprite sprite = new Sprite("ignored.png");
        sprite.r = 0.2f;
        sprite.g = 0.4f;
        sprite.b = 0.6f;
        sprite.a = 0.8f;
        Scene scene = new Scene("test").add(new Node("head").add(new Transform2D(3f, 5f)).add(sprite));

        SceneShapeRenderer renderer = SceneShapeRenderer.recordingOnly();
        renderer.render(scene);

        List<RectDraw> expected = SceneWalk.collectDrawCommands(scene);
        List<ShapeDrawCommand> actual = renderer.lastDrawCommands();
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).x, actual.get(0).x, 0.001f);
        assertEquals(expected.get(0).y, actual.get(0).y, 0.001f);
        assertEquals(expected.get(0).width, actual.get(0).width, 0.001f);
        assertEquals(expected.get(0).height, actual.get(0).height, 0.001f);
        assertEquals(expected.get(0).r, actual.get(0).r, 0.001f);
        assertEquals(expected.get(0).g, actual.get(0).g, 0.001f);
        assertEquals(expected.get(0).b, actual.get(0).b, 0.001f);
        assertEquals(expected.get(0).a, actual.get(0).a, 0.001f);
    }

    @Test
    void renderEmptySceneYieldsEmptyLastDrawCommands() {
        SceneShapeRenderer renderer = SceneShapeRenderer.recordingOnly();

        renderer.render(new Scene("empty"));

        assertTrue(renderer.lastDrawCommands().isEmpty());
    }

    @Test
    void disposeIsIdempotentWhenRecordingOnly() {
        SceneShapeRenderer renderer = SceneShapeRenderer.recordingOnly();

        renderer.dispose();
        renderer.dispose();

        assertTrue(renderer.isDisposed());
    }
}
