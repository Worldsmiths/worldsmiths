package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.Graphics;

/**
 * GDX-backed {@link Graphics}. {@link #setTitle(String)} is best-effort — some backends ignore it.
 * Not part of the author-facing API.
 */
final class GdxGraphics implements Graphics {

    @Override
    public int getWidth() {
        return Gdx.graphics.getWidth();
    }

    @Override
    public int getHeight() {
        return Gdx.graphics.getHeight();
    }

    @Override
    public void setTitle(String title) {
        try {
            Gdx.graphics.setTitle(title);
        } catch (Throwable ignored) {
            // Best-effort: Android / headless may throw or no-op; never fail the game for a title.
        }
    }
}
