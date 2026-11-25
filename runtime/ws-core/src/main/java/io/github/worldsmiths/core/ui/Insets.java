package io.github.worldsmiths.core.ui;

/** Viewport safe-area insets (logical px). Default 0 until a platform feeds them. */
public final class Insets {
    public static final Insets ZERO = new Insets(0, 0, 0, 0);

    public final int top;
    public final int left;
    public final int bottom;
    public final int right;

    public Insets(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }
}