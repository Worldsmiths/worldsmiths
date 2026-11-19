package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Font;
import io.github.worldsmiths.core.Ui;

/**
 * GDX-backed {@link Ui} loading bitmap fonts via internal files. Each {@link Font} owns its
 * SpriteBatch (created at {@link #createFont}, disposed with the font). SpriteBatch stays hidden
 * inside this package — not part of the author-facing API. Backends assemble this via {@link
 * GdxEngineServices}.
 */
final class GdxUi implements Ui {
    private final FontOpener opener;
    private final FontBatchFactory batchFactory;

    GdxUi() {
        this(
                new FontOpener() {
                    @Override
                    public LoadedBitmapFont open(String path, FontBatch fontBatch) {
                        FileHandle handle = Gdx.files.internal(path);
                        if (handle == null || !handle.exists()) {
                            return null;
                        }
                        final BitmapFont font = new BitmapFont(handle);
                        final Batch batch = fontBatch.gdxBatch();
                        return new LoadedBitmapFont() {
                            @Override
                            public void draw(String text, float x, float y) {
                                font.draw(batch, text, x, y);
                            }

                            @Override
                            public void dispose() {
                                font.dispose();
                            }
                        };
                    }
                },
                SpriteFontBatch.FACTORY);
    }

    /**
     * Package-visible for tests that inject fonts without a live Application / display. Uses a
     * no-op {@link FontBatch} (no GL).
     */
    GdxUi(FontOpener opener) {
        this(opener, NoopFontBatch.FACTORY);
    }

    /**
     * Package-visible for tests that also inject a {@link FontBatchFactory} (e.g. to assert batch
     * dispose).
     */
    GdxUi(FontOpener opener, FontBatchFactory batchFactory) {
        this.opener = opener;
        this.batchFactory = batchFactory;
    }

    @Override
    public Font createFont(String internalPath) {
        FontBatch batch = batchFactory.create();
        LoadedBitmapFont gdxFont;
        try {
            gdxFont = opener.open(internalPath, batch);
        } catch (RuntimeException e) {
            batch.dispose();
            throw new EngineException("Failed to load font: " + internalPath, e);
        }
        if (gdxFont == null) {
            batch.dispose();
            throw new EngineException("Missing or unloadable font: " + internalPath);
        }
        return new GdxFont(gdxFont, batch);
    }

    /**
     * Resolves an internal path to a loaded LibGDX bitmap font. Return {@code null} when the path
     * is missing; throw on unloadable decode failures. {@code fontBatch} is the font-owned batch —
     * production closes over {@link FontBatch#gdxBatch()} for {@link BitmapFont#draw}; test stubs
     * may ignore it.
     */
    interface FontOpener {
        LoadedBitmapFont open(String path, FontBatch fontBatch);
    }

    /**
     * Thin seam over {@link BitmapFont} so unit tests can inject recording stubs without
     * constructing a real font (which needs GL textures). Draw takes text/position only — the
     * production wrap closes over BitmapFont and the font-owned batch.
     */
    interface LoadedBitmapFont {
        void draw(String text, float x, float y);

        void dispose();
    }

    /**
     * Narrow batch surface used by {@link GdxFont} — begin/end/ortho/dispose only. Avoids requiring
     * the full LibGDX {@link Batch} interface in tests. Production {@link #gdxBatch()} returns the
     * underlying {@link SpriteBatch}.
     */
    interface FontBatch {
        void begin();

        void end();

        void setProjectionMatrix(Matrix4 matrix);

        void dispose();

        /** Underlying LibGDX batch for {@link BitmapFont#draw}; may be {@code null} in tests. */
        Batch gdxBatch();
    }

    /** Creates a font-owned {@link FontBatch} per {@link #createFont}. */
    interface FontBatchFactory {
        FontBatch create();
    }

    private static final class SpriteFontBatch implements FontBatch {
        static final FontBatchFactory FACTORY =
                new FontBatchFactory() {
                    @Override
                    public FontBatch create() {
                        return new SpriteFontBatch(new SpriteBatch());
                    }
                };

        private final SpriteBatch sprite;

        SpriteFontBatch(SpriteBatch sprite) {
            this.sprite = sprite;
        }

        @Override
        public void begin() {
            sprite.begin();
        }

        @Override
        public void end() {
            sprite.end();
        }

        @Override
        public void setProjectionMatrix(Matrix4 matrix) {
            sprite.setProjectionMatrix(matrix);
        }

        @Override
        public void dispose() {
            sprite.dispose();
        }

        @Override
        public Batch gdxBatch() {
            return sprite;
        }
    }

    private static final class NoopFontBatch implements FontBatch {
        static final FontBatchFactory FACTORY =
                new FontBatchFactory() {
                    @Override
                    public FontBatch create() {
                        return new NoopFontBatch();
                    }
                };

        @Override
        public void begin() {}

        @Override
        public void end() {}

        @Override
        public void setProjectionMatrix(Matrix4 matrix) {}

        @Override
        public void dispose() {}

        @Override
        public Batch gdxBatch() {
            return null;
        }
    }

    private static final class GdxFont implements Font {
        private final LoadedBitmapFont delegate;
        private final FontBatch batch;
        private boolean disposed;

        GdxFont(LoadedBitmapFont delegate, FontBatch batch) {
            this.delegate = delegate;
            this.batch = batch;
        }

        @Override
        public void drawString(String text, float x, float y) {
            try {
                applyOrtho();
                batch.begin();
                try {
                    delegate.draw(text, x, y);
                } finally {
                    batch.end();
                }
            } catch (RuntimeException ignored) {
                // best-effort — never fail the game
            }
        }

        @Override
        public void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            delegate.dispose();
            batch.dispose();
        }

        private void applyOrtho() {
            if (Gdx.graphics == null) {
                return;
            }
            batch.setProjectionMatrix(
                    new Matrix4()
                            .setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        }
    }
}
