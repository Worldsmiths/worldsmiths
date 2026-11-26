package io.github.worldsmiths.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.RectDraw;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneWalk;
import io.github.worldsmiths.core.gdx.GdxRetainedSceneDraw;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Thin adapter: walks a retained {@link Scene} via {@link SceneWalk} and draws via shared {@link
 * GdxRetainedSceneDraw} (filled rects and textured quads). Owns the drawer when constructed for GL;
 * dispose when the listener shuts down.
 */
public final class SceneShapeRenderer {

    /** Pixel size of one scene cell; aliases {@link SceneWalk#CELL_SIZE}. */
    public static final float CELL_SIZE = SceneWalk.CELL_SIZE;

    private final GdxRetainedSceneDraw drawer;
    private final IntSupplier screenWidth;
    private final IntSupplier screenHeight;
    private List<ShapeDrawCommand> lastDrawCommands = Collections.emptyList();
    private Matrix4 lastProjectionMatrix;
    private float lastProjectionWidth = -1f;
    private float lastProjectionHeight = -1f;
    private boolean disposed;

    /**
     * Production renderer using the shared retained-scene drawer (textures cache + ShapeRenderer /
     * SpriteBatch).
     */
    public SceneShapeRenderer(GdxRetainedSceneDraw drawer) {
        this(
                drawer,
                new IntSupplier() {
                    @Override
                    public int getAsInt() {
                        return Gdx.graphics.getWidth();
                    }
                },
                new IntSupplier() {
                    @Override
                    public int getAsInt() {
                        return Gdx.graphics.getHeight();
                    }
                });
    }

    /**
     * Recording-only instance: walks the scene and stores draw commands without touching GL.
     * Package-visible test seam used by {@link DesktopGameListenerTest}.
     */
    static SceneShapeRenderer recordingOnly() {
        return new SceneShapeRenderer(
                null,
                new IntSupplier() {
                    @Override
                    public int getAsInt() {
                        return Gdx.graphics.getWidth();
                    }
                },
                new IntSupplier() {
                    @Override
                    public int getAsInt() {
                        return Gdx.graphics.getHeight();
                    }
                });
    }

    /**
     * Package-visible for tests that inject screen size without {@link Gdx#graphics}.
     * Recording-only callers pass {@code drawer == null} so suppliers are never read on render.
     */
    SceneShapeRenderer(
            GdxRetainedSceneDraw drawer, IntSupplier screenWidth, IntSupplier screenHeight) {
        this.drawer = drawer;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * Maps {@link SceneWalk#collectDrawCommands(Scene)} into local {@link ShapeDrawCommand}s
     * without touching GL. Package-visible test seam and shared path used by {@link #render(Scene)}.
     */
    static List<ShapeDrawCommand> collectDrawCommands(Scene scene) {
        List<RectDraw> rects = SceneWalk.collectDrawCommands(scene);
        List<ShapeDrawCommand> draws = new ArrayList<ShapeDrawCommand>(rects.size());
        for (RectDraw rect : rects) {
            draws.add(
                    new ShapeDrawCommand(
                            rect.x,
                            rect.y,
                            rect.width,
                            rect.height,
                            rect.r,
                            rect.g,
                            rect.b,
                            rect.a,
                            rect.textureKey));
        }
        return draws;
    }

    /**
     * Screen-space ortho matrix for pixel coordinates (origin bottom-left). Package-visible test
     * seam shared with the GL draw path.
     */
    static Matrix4 screenOrthoMatrix(float width, float height) {
        return new Matrix4().setToOrtho2D(0f, 0f, width, height);
    }

    /**
     * Records and returns the screen ortho used before draws. Package-visible test seam; the GL
     * path calls this then passes the matrix to {@link GdxRetainedSceneDraw#draw}.
     */
    Matrix4 prepareScreenProjection(float width, float height) {
        lastProjectionWidth = width;
        lastProjectionHeight = height;
        lastProjectionMatrix = screenOrthoMatrix(width, height);
        return lastProjectionMatrix;
    }

    /**
     * Draws all visible sprites in {@code scene} (colored rects and/or textured quads), or records
     * them when headless.
     */
    public void render(Scene scene) {
        List<RectDraw> rects = SceneWalk.collectDrawCommands(scene);
        lastDrawCommands = collectDrawCommands(scene);
        if (drawer == null || rects.isEmpty()) {
            return;
        }
        drawer.draw(
                rects, prepareScreenProjection(screenWidth.getAsInt(), screenHeight.getAsInt()));
    }

    /** Commands from the most recent {@link #render(Scene)}. Package-visible for unit tests. */
    List<ShapeDrawCommand> lastDrawCommands() {
        return lastDrawCommands;
    }

    /** Last ortho width passed to {@link #prepareScreenProjection}; {@code -1} if never applied. */
    float lastProjectionWidth() {
        return lastProjectionWidth;
    }

    /** Last ortho height passed to {@link #prepareScreenProjection}; {@code -1} if never applied. */
    float lastProjectionHeight() {
        return lastProjectionHeight;
    }

    /** Matrix from the most recent {@link #prepareScreenProjection}; null if never applied. */
    Matrix4 lastProjectionMatrix() {
        return lastProjectionMatrix;
    }

    /** Whether {@link #dispose()} has been called. Package-visible for unit tests. */
    boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (drawer != null) {
            drawer.dispose();
        }
    }
}
