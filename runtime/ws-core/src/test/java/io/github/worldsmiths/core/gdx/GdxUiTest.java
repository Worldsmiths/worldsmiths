package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Headless GdxUi coverage via an injected {@link GdxUi.FontOpener} and {@link Batch} — no
 * Application / display required.
 */
class GdxUiTest {

    @Test
    void createsDrawsAndDisposesViaInjectedOpener() {
        RecordingFont gdxFont = new RecordingFont();
        Map<String, GdxUi.LoadedBitmapFont> catalog =
                new HashMap<String, GdxUi.LoadedBitmapFont>();
        catalog.put("fonts/hud.fnt", gdxFont);
        RecordingBatch batch = new RecordingBatch();

        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(String path) {
                                return catalog.get(path);
                            }
                        },
                        batch);

        Font font = ui.createFont("fonts/hud.fnt");
        font.drawString("1:23", 8f, 24f);
        font.dispose();

        assertEquals(java.util.Arrays.asList("begin", "end"), batch.calls);
        assertEquals(java.util.Arrays.asList("draw:1:23@8.0,24.0", "dispose"), gdxFont.calls);
    }

    @Test
    void missingInternalPathThrowsEngineException() {
        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(String path) {
                                return null;
                            }
                        },
                        new RecordingBatch());

        EngineException thrown =
                assertThrows(EngineException.class, () -> ui.createFont("gone.fnt"));
        assertTrue(thrown.getMessage().contains("gone.fnt"));
    }

    @Test
    void unloadableOpenThrowsEngineException() {
        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(String path) {
                                throw new IllegalStateException("decode failed");
                            }
                        },
                        new RecordingBatch());

        EngineException thrown =
                assertThrows(EngineException.class, () -> ui.createFont("bad.fnt"));
        assertTrue(thrown.getMessage().contains("bad.fnt"));
        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }

    @Test
    void drawStringSwallowsDelegateFailures() {
        FailingFont gdxFont = new FailingFont();
        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(String path) {
                                return gdxFont;
                            }
                        },
                        new RecordingBatch());

        Font font = ui.createFont("fonts/hud.fnt");
        font.drawString("boom", 0f, 0f);
        assertEquals(1, gdxFont.attempts);
    }

    @Test
    void disposeIsIdempotentAndCallsDelegateOnce() {
        RecordingFont gdxFont = new RecordingFont();
        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(String path) {
                                return gdxFont;
                            }
                        },
                        new RecordingBatch());

        Font font = ui.createFont("fonts/hud.fnt");
        font.dispose();
        font.dispose();

        assertEquals(java.util.Arrays.asList("dispose"), gdxFont.calls);
    }

    private static final class RecordingFont implements GdxUi.LoadedBitmapFont {
        final List<String> calls = new ArrayList<String>();

        @Override
        public void draw(Batch batch, String text, float x, float y) {
            calls.add("draw:" + text + "@" + x + "," + y);
        }

        @Override
        public void dispose() {
            calls.add("dispose");
        }
    }

    private static final class FailingFont implements GdxUi.LoadedBitmapFont {
        int attempts;

        @Override
        public void draw(Batch batch, String text, float x, float y) {
            attempts++;
            throw new IllegalStateException("gpu busy");
        }

        @Override
        public void dispose() {}
    }

    private static final class RecordingBatch implements Batch {
        final List<String> calls = new ArrayList<String>();
        private final Matrix4 projection = new Matrix4();
        private final Matrix4 transform = new Matrix4();
        private final Color color = new Color(1f, 1f, 1f, 1f);
        private boolean drawing;

        @Override
        public void begin() {
            calls.add("begin");
            drawing = true;
        }

        @Override
        public void end() {
            calls.add("end");
            drawing = false;
        }

        @Override
        public void setColor(Color tint) {
            color.set(tint);
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            color.set(r, g, b, a);
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public void setPackedColor(float packedColor) {}

        @Override
        public float getPackedColor() {
            return color.toFloatBits();
        }

        @Override
        public void draw(
                Texture texture,
                float x,
                float y,
                float originX,
                float originY,
                float width,
                float height,
                float scaleX,
                float scaleY,
                float rotation,
                int srcX,
                int srcY,
                int srcWidth,
                int srcHeight,
                boolean flipX,
                boolean flipY) {}

        @Override
        public void draw(
                Texture texture,
                float x,
                float y,
                float width,
                float height,
                int srcX,
                int srcY,
                int srcWidth,
                int srcHeight,
                boolean flipX,
                boolean flipY) {}

        @Override
        public void draw(
                Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {}

        @Override
        public void draw(
                Texture texture,
                float x,
                float y,
                float width,
                float height,
                float u,
                float v,
                float u2,
                float v2) {}

        @Override
        public void draw(Texture texture, float x, float y) {}

        @Override
        public void draw(Texture texture, float x, float y, float width, float height) {}

        @Override
        public void draw(Texture texture, float[] spriteVertices, int offset, int count) {}

        @Override
        public void draw(TextureRegion region, float x, float y) {}

        @Override
        public void draw(TextureRegion region, float x, float y, float width, float height) {}

        @Override
        public void draw(
                TextureRegion region,
                float x,
                float y,
                float originX,
                float originY,
                float width,
                float height,
                float scaleX,
                float scaleY,
                float rotation) {}

        @Override
        public void draw(
                TextureRegion region,
                float x,
                float y,
                float originX,
                float originY,
                float width,
                float height,
                float scaleX,
                float scaleY,
                float rotation,
                boolean clockwise) {}

        @Override
        public void draw(TextureRegion region, float width, float height, Affine2 transform) {}

        @Override
        public void flush() {}

        @Override
        public void disableBlending() {}

        @Override
        public void enableBlending() {}

        @Override
        public void setBlendFunction(int srcFunc, int dstFunc) {}

        @Override
        public void setBlendFunctionSeparate(
                int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {}

        @Override
        public int getBlendSrcFunc() {
            return 0;
        }

        @Override
        public int getBlendDstFunc() {
            return 0;
        }

        @Override
        public int getBlendSrcFuncAlpha() {
            return 0;
        }

        @Override
        public int getBlendDstFuncAlpha() {
            return 0;
        }

        @Override
        public Matrix4 getProjectionMatrix() {
            return projection;
        }

        @Override
        public Matrix4 getTransformMatrix() {
            return transform;
        }

        @Override
        public void setProjectionMatrix(Matrix4 projection) {
            this.projection.set(projection);
        }

        @Override
        public void setTransformMatrix(Matrix4 transform) {
            this.transform.set(transform);
        }

        @Override
        public void setShader(ShaderProgram shader) {}

        @Override
        public ShaderProgram getShader() {
            return null;
        }

        @Override
        public boolean isBlendingEnabled() {
            return false;
        }

        @Override
        public boolean isDrawing() {
            return drawing;
        }

        @Override
        public void dispose() {}
    }
}
