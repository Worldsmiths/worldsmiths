package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Headless GdxUi coverage via an injected {@link GdxUi.FontOpener} and {@link GdxUi.FontBatch} —
 * no Application / display required, and no LibGDX {@code Batch} stub.
 */
class GdxUiTest {

    @Test
    void createsDrawsAndDisposesViaInjectedOpener() {
        RecordingFont gdxFont = new RecordingFont();
        Map<String, GdxUi.LoadedBitmapFont> catalog =
                new HashMap<String, GdxUi.LoadedBitmapFont>();
        catalog.put("fonts/hud.fnt", gdxFont);
        RecordingFontBatch batch = new RecordingFontBatch();

        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(
                                    String path, GdxUi.FontBatch ignored) {
                                return catalog.get(path);
                            }
                        },
                        new GdxUi.FontBatchFactory() {
                            @Override
                            public GdxUi.FontBatch create() {
                                return batch;
                            }
                        });

        Font font = ui.createFont("fonts/hud.fnt");
        font.drawString("1:23", 8f, 24f);
        font.dispose();

        assertEquals(java.util.Arrays.asList("begin", "end", "dispose"), batch.calls);
        assertEquals(java.util.Arrays.asList("draw:1:23@8.0,24.0", "dispose"), gdxFont.calls);
    }

    @Test
    void missingInternalPathThrowsEngineException() {
        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(
                                    String path, GdxUi.FontBatch ignored) {
                                return null;
                            }
                        });

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
                            public GdxUi.LoadedBitmapFont open(
                                    String path, GdxUi.FontBatch ignored) {
                                throw new IllegalStateException("decode failed");
                            }
                        });

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
                            public GdxUi.LoadedBitmapFont open(
                                    String path, GdxUi.FontBatch ignored) {
                                return gdxFont;
                            }
                        });

        Font font = ui.createFont("fonts/hud.fnt");
        font.drawString("boom", 0f, 0f);
        assertEquals(1, gdxFont.attempts);
    }

    @Test
    void disposeIsIdempotentAndDisposesFontThenBatchOnce() {
        RecordingFont gdxFont = new RecordingFont();
        RecordingFontBatch batch = new RecordingFontBatch();
        GdxUi ui =
                new GdxUi(
                        new GdxUi.FontOpener() {
                            @Override
                            public GdxUi.LoadedBitmapFont open(
                                    String path, GdxUi.FontBatch ignored) {
                                return gdxFont;
                            }
                        },
                        new GdxUi.FontBatchFactory() {
                            @Override
                            public GdxUi.FontBatch create() {
                                return batch;
                            }
                        });

        Font font = ui.createFont("fonts/hud.fnt");
        font.dispose();
        font.dispose();

        assertEquals(java.util.Arrays.asList("dispose"), gdxFont.calls);
        assertEquals(java.util.Arrays.asList("dispose"), batch.calls);
    }

    private static final class RecordingFont implements GdxUi.LoadedBitmapFont {
        final List<String> calls = new ArrayList<String>();

        @Override
        public void draw(String text, float x, float y) {
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
        public void draw(String text, float x, float y) {
            attempts++;
            throw new IllegalStateException("gpu busy");
        }

        @Override
        public void dispose() {}
    }

    private static final class RecordingFontBatch implements GdxUi.FontBatch {
        final List<String> calls = new ArrayList<String>();

        @Override
        public void begin() {
            calls.add("begin");
        }

        @Override
        public void end() {
            calls.add("end");
        }

        @Override
        public void setProjectionMatrix(Matrix4 matrix) {}

        @Override
        public void dispose() {
            calls.add("dispose");
        }

        @Override
        public com.badlogic.gdx.graphics.g2d.Batch gdxBatch() {
            return null;
        }
    }
}
