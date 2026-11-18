package io.github.worldsmiths.core;

/**
 * Imperative immediate-mode 2D drawing for overlays (grid lines, debug shapes). No LibGDX types in
 * the contract. Retained sprites still go through {@link SceneWalk} / backend scene renderers.
 *
 * <p>Typical frame: {@link #setOrtho2D} → {@link #begin} → {@link #setColor} / {@link #line} /
 * {@link #rect} → {@link #end}.
 *
 * <p><b>Ownership:</b> the bag's {@code Draw2D} is bag-owned. Games call {@link #dispose()} once at
 * shutdown via {@code services.draw2d().dispose()} (GDX-backed bags own a ShapeRenderer).
 */
public interface Draw2D {

    /**
     * Screen-space ortho for pixel coordinates (origin bottom-left). Equivalent to a 2D orthographic
     * projection covering {@code [x, x+width] × [y, y+height]}.
     */
    void setOrtho2D(float x, float y, float width, float height);

    /** Current draw color for subsequent {@link #line} / {@link #rect} calls. */
    void setColor(float r, float g, float b, float a);

    /** Starts a draw batch in {@code mode}. Must be paired with {@link #end()}. */
    void begin(DrawMode mode);

    void line(float x1, float y1, float x2, float y2);

    void rect(float x, float y, float width, float height);

    /** Ends the current {@link #begin(DrawMode)} batch. */
    void end();

    /**
     * Releases backend resources. Safe to call more than once. Fake/recording impls are no-ops.
     */
    void dispose();
}
