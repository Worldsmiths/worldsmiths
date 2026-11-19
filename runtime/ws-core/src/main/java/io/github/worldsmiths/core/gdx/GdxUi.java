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
 * GDX-backed {@link Ui} loading bitmap fonts via internal files. SpriteBatch stays hidden inside
 * this package — not part of the author-facing API. Backends assemble this via {@link
 * GdxEngineServices}.
 */
final class GdxUi implements Ui {
    private final FontOpener opener;
    private final Batch batch;

    GdxUi() {
        this(
                new FontOpener() {
                    @Override
                    public LoadedBitmapFont open(String path) {
                        FileHandle handle = Gdx.files.internal(path);
                        if (handle == null || !handle.exists()) {
                            return null;
                        }
                        final BitmapFont font = new BitmapFont(handle);
                        return new LoadedBitmapFont() {
                            @Override
                            public void draw(Batch batch, String text, float x, float y) {
                                font.draw(batch, text, x, y);
                            }

                            @Override
                            public void dispose() {
                                font.dispose();
                            }
                        };
                    }
                },
                new SpriteBatch());
    }

    /**
     * Package-visible for tests that inject fonts / a batch without a live Application / display.
     * Production {@link FontOpener} loads a LibGDX {@link BitmapFont}; the opener returns a thin
     * {@link LoadedBitmapFont} so headless tests can stub draw/dispose without GL (BitmapFont
     * construction needs textures).
     */
    GdxUi(FontOpener opener, Batch batch) {
        this.opener = opener;
        this.batch = batch;
    }

    @Override
    public Font createFont(String internalPath) {
        LoadedBitmapFont gdxFont;
        try {
            gdxFont = opener.open(internalPath);
        } catch (RuntimeException e) {
            throw new EngineException("Failed to load font: " + internalPath, e);
        }
        if (gdxFont == null) {
            throw new EngineException("Missing or unloadable font: " + internalPath);
        }
        return new GdxFont(gdxFont, batch);
    }

    /**
     * Resolves an internal path to a loaded LibGDX bitmap font. Return {@code null} when the path
     * is missing; throw on unloadable decode failures.
     */
    interface FontOpener {
        LoadedBitmapFont open(String path);
    }

    /**
     * Thin seam over {@link BitmapFont} so unit tests can inject recording stubs without
     * constructing a real font (which needs GL textures).
     */
    interface LoadedBitmapFont {
        void draw(Batch batch, String text, float x, float y);

        void dispose();
    }

    private static final class GdxFont implements Font {
        private final LoadedBitmapFont delegate;
        private final Batch batch;
        private boolean disposed;

        GdxFont(LoadedBitmapFont delegate, Batch batch) {
            this.delegate = delegate;
            this.batch = batch;
        }

        @Override
        public void drawString(String text, float x, float y) {
            try {
                applyOrtho();
                batch.begin();
                try {
                    delegate.draw(batch, text, x, y);
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
