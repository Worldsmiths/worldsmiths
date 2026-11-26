package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.RectDraw;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/**
 * Headless retained-scene draw coverage via injected shape/sprite/texture seams — no Application /
 * display / GL context required.
 */
class GdxRetainedSceneDrawTest {

    @Test
    void nullTextureKeyDrawsFilledRectWithoutTextureLookup() {
        RecordingShapeBatch shapes = new RecordingShapeBatch();
        RecordingSpriteBatch sprites = new RecordingSpriteBatch();
        AtomicBoolean lookedUp = new AtomicBoolean();
        GdxRetainedSceneDraw drawer =
                new GdxRetainedSceneDraw(
                        shapes,
                        sprites,
                        new GdxRetainedSceneDraw.TextureLookup() {
                            @Override
                            public Texture require(String path) {
                                lookedUp.set(true);
                                fail("null texture key must not hit the cache");
                                return null;
                            }

                            @Override
                            public boolean isAsyncInFlight(String path) {
                                return false;
                            }
                        });

        drawer.draw(
                Collections.singletonList(new RectDraw(10f, 20f, 32f, 32f, 0.2f, 0.4f, 0.6f, 0.8f)),
                new Matrix4().setToOrtho2D(0f, 0f, 640f, 480f));

        assertFalse(lookedUp.get());
        assertEquals(
                Arrays.asList(
                        "setProjection",
                        "beginFilled",
                        "setColor:0.2,0.4,0.6,0.8",
                        "rect:10.0,20.0,32.0,32.0",
                        "end"),
                shapes.calls);
        assertTrue(sprites.calls.isEmpty());
    }

    @Test
    void setTextureKeyDrawsTintedQuadFromCache() {
        RecordingShapeBatch shapes = new RecordingShapeBatch();
        RecordingSpriteBatch sprites = new RecordingSpriteBatch();
        final List<String> lookups = new ArrayList<String>();
        final Texture stubTexture = null;
        GdxRetainedSceneDraw drawer =
                new GdxRetainedSceneDraw(
                        shapes,
                        sprites,
                        new GdxRetainedSceneDraw.TextureLookup() {
                            @Override
                            public Texture require(String path) {
                                lookups.add(path);
                                return stubTexture;
                            }

                            @Override
                            public boolean isAsyncInFlight(String path) {
                                return false;
                            }
                        });

        drawer.draw(
                Collections.singletonList(
                        new RectDraw(8f, 16f, 32f, 32f, 1f, 0.5f, 0.25f, 0.75f, "hero.png")),
                new Matrix4().setToOrtho2D(0f, 0f, 640f, 480f));

        assertEquals(Collections.singletonList("hero.png"), lookups);
        assertTrue(shapes.calls.isEmpty());
        assertEquals(
                Arrays.asList(
                        "setProjection",
                        "begin",
                        "setColor:1.0,0.5,0.25,0.75",
                        "draw:8.0,16.0,32.0,32.0",
                        "end"),
                sprites.calls);
    }

    @Test
    void inFlightAsyncTextureKeySkipsWithoutCallingRequire() {
        RecordingShapeBatch shapes = new RecordingShapeBatch();
        RecordingSpriteBatch sprites = new RecordingSpriteBatch();
        final AtomicBoolean required = new AtomicBoolean();
        GdxRetainedSceneDraw drawer =
                new GdxRetainedSceneDraw(
                        shapes,
                        sprites,
                        new GdxRetainedSceneDraw.TextureLookup() {
                            @Override
                            public Texture require(String path) {
                                required.set(true);
                                fail("in-flight async key must not sync-require");
                                return null;
                            }

                            @Override
                            public boolean isAsyncInFlight(String path) {
                                return "textures/heavy.png".equals(path);
                            }
                        });

        drawer.draw(
                Collections.singletonList(
                        new RectDraw(8f, 16f, 32f, 32f, 1f, 0.5f, 0.25f, 0.75f, "textures/heavy.png")),
                new Matrix4().setToOrtho2D(0f, 0f, 640f, 480f));

        assertFalse(required.get());
        assertTrue(sprites.calls.isEmpty());
        assertTrue(shapes.calls.isEmpty());
    }

    @Test
    void mixedCommandsEndOneBatcherBeforeBeginningTheOther() {
        RecordingShapeBatch shapes = new RecordingShapeBatch();
        RecordingSpriteBatch sprites = new RecordingSpriteBatch();
        GdxRetainedSceneDraw drawer =
                new GdxRetainedSceneDraw(
                        shapes,
                        sprites,
                        new GdxRetainedSceneDraw.TextureLookup() {
                            @Override
                            public Texture require(String path) {
                                return null;
                            }

                            @Override
                            public boolean isAsyncInFlight(String path) {
                                return false;
                            }
                        });

        List<RectDraw> commands =
                Arrays.asList(
                        new RectDraw(0f, 0f, 10f, 10f, 1f, 0f, 0f, 1f),
                        new RectDraw(10f, 0f, 10f, 10f, 1f, 1f, 1f, 1f, "a.png"),
                        new RectDraw(20f, 0f, 10f, 10f, 0f, 1f, 0f, 1f));

        drawer.draw(commands, new Matrix4().setToOrtho2D(0f, 0f, 100f, 100f));

        assertEquals(
                Arrays.asList(
                        "setProjection",
                        "beginFilled",
                        "setColor:1.0,0.0,0.0,1.0",
                        "rect:0.0,0.0,10.0,10.0",
                        "end",
                        "setProjection",
                        "beginFilled",
                        "setColor:0.0,1.0,0.0,1.0",
                        "rect:20.0,0.0,10.0,10.0",
                        "end"),
                shapes.calls);
        assertEquals(
                Arrays.asList(
                        "setProjection",
                        "begin",
                        "setColor:1.0,1.0,1.0,1.0",
                        "draw:10.0,0.0,10.0,10.0",
                        "end"),
                sprites.calls);
        assertFalse(shapes.isDrawing());
        assertFalse(sprites.isDrawing());
    }

    private static final class RecordingShapeBatch implements GdxRetainedSceneDraw.ShapeBatch {
        final List<String> calls = new ArrayList<String>();
        private boolean drawing;

        @Override
        public void setProjectionMatrix(Matrix4 matrix) {
            calls.add("setProjection");
        }

        @Override
        public void beginFilled() {
            drawing = true;
            calls.add("beginFilled");
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            calls.add("setColor:" + r + "," + g + "," + b + "," + a);
        }

        @Override
        public void rect(float x, float y, float width, float height) {
            calls.add("rect:" + x + "," + y + "," + width + "," + height);
        }

        @Override
        public void end() {
            drawing = false;
            calls.add("end");
        }

        @Override
        public void dispose() {
            calls.add("dispose");
        }

        @Override
        public boolean isDrawing() {
            return drawing;
        }
    }

    private static final class RecordingSpriteBatch implements GdxRetainedSceneDraw.SpriteBatcher {
        final List<String> calls = new ArrayList<String>();
        private boolean drawing;

        @Override
        public void setProjectionMatrix(Matrix4 matrix) {
            calls.add("setProjection");
        }

        @Override
        public void begin() {
            drawing = true;
            calls.add("begin");
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            calls.add("setColor:" + r + "," + g + "," + b + "," + a);
        }

        @Override
        public void draw(Texture texture, float x, float y, float width, float height) {
            calls.add("draw:" + x + "," + y + "," + width + "," + height);
        }

        @Override
        public void end() {
            drawing = false;
            calls.add("end");
        }

        @Override
        public void dispose() {
            calls.add("dispose");
        }

        @Override
        public boolean isDrawing() {
            return drawing;
        }
    }
}
