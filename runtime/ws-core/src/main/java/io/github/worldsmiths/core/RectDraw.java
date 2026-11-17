package io.github.worldsmiths.core;

/**
 * One filled-rect draw command produced by walking a {@link Scene}. Plain floats only — no
 * libGDX draw types.
 */
public final class RectDraw {

    public final float x;
    public final float y;
    public final float width;
    public final float height;
    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public RectDraw(
            float x, float y, float width, float height, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
