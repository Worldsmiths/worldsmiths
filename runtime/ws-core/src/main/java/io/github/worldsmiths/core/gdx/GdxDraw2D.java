package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.DrawMode;

/**
 * GDX-backed {@link Draw2D} owning a {@link ShapeRenderer}. Not part of the author-facing API.
 * Games obtain this via {@link io.github.worldsmiths.core.EngineServices#draw2d()} and must call
 * {@link #dispose()} at shutdown.
 */
final class GdxDraw2D implements Draw2D {
    private final ShapeRenderer shapes;
    private boolean disposed;

    GdxDraw2D() {
        this(new ShapeRenderer());
    }

    /** Package-visible for tests that inject a fake or already-constructed renderer. */
    GdxDraw2D(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    @Override
    public void setOrtho2D(float x, float y, float width, float height) {
        shapes.setProjectionMatrix(new Matrix4().setToOrtho2D(x, y, width, height));
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        shapes.setColor(r, g, b, a);
    }

    @Override
    public void begin(DrawMode mode) {
        shapes.begin(toShapeType(mode));
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        shapes.line(x1, y1, x2, y2);
    }

    @Override
    public void rect(float x, float y, float width, float height) {
        shapes.rect(x, y, width, height);
    }

    @Override
    public void end() {
        shapes.end();
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        shapes.dispose();
    }

    private static ShapeRenderer.ShapeType toShapeType(DrawMode mode) {
        if (mode == DrawMode.FILLED) {
            return ShapeRenderer.ShapeType.Filled;
        }
        return ShapeRenderer.ShapeType.Line;
    }
}
