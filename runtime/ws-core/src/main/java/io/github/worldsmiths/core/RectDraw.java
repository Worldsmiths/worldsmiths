package io.github.worldsmiths.core;

/**
 * One filled-rect draw command produced by walking a {@link Scene}. Plain floats only — no
 * libGDX draw types. Optional {@link #textureKey} selects a textured quad when non-null.
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
    /** Non-blank sprite texture key, or null for a colored rect. */
    public final String textureKey;

    /** Colored-rect command with no texture key (compatible with existing callers). */
    public RectDraw(
            float x, float y, float width, float height, float r, float g, float b, float a) {
        this(x, y, width, height, r, g, b, a, null);
    }

    public RectDraw(
            float x,
            float y,
            float width,
            float height,
            float r,
            float g,
            float b,
            float a,
            String textureKey) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.textureKey = textureKey;
    }
}
