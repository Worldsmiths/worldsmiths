package io.github.worldsmiths.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.RectDraw;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneWalk;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Thin ShapeRenderer adapter: walks a retained {@link Scene} via {@link SceneWalk} and draws each
 * rect as a filled shape. Owns a libGDX {@link ShapeRenderer}; dispose when the listener shuts
 * down.
 */
public final class SceneShapeRenderer {

    /** Pixel size of one scene cell; aliases {@link SceneWalk#CELL_SIZE}. */
    public static final float CELL_SIZE = SceneWalk.CELL_SIZE;

    private final ShapeRenderer shapes;
    private final IntSupplier screenWidth;
    private final IntSupplier screenHeight;
    private List<ShapeDrawCommand> lastDrawCommands = Collections.emptyList();
    private Matrix4 lastProjectionMatrix;
    private float lastProjectionWidth = -1f;
    private float lastProjectionHeight = -1f;
    private boolean disposed;

    public SceneShapeRenderer() {
        this(
                new ShapeRenderer(),
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
     * Package-visible test seam used by {@link AndroidGameListenerTest}.
     */
    static SceneShapeRenderer recordingOnly() {
        return new SceneShapeRenderer(null);
    }

    /** Package-visible for tests that inject a fake or already-constructed renderer. */
    SceneShapeRenderer(ShapeRenderer shapes) {
        this(
                shapes,
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
     * Recording-only callers pass {@code shapes == null} so suppliers are never read on render.
     */
    SceneShapeRenderer(ShapeRenderer shapes, IntSupplier screenWidth, IntSupplier screenHeight) {
        this.shapes = shapes;
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
                            rect.a));
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
     * Records and returns the screen ortho used before ShapeRenderer draws. Package-visible test
     * seam; the GL path calls this then {@link ShapeRenderer#setProjectionMatrix(Matrix4)}.
     */
    Matrix4 prepareScreenProjection(float width, float height) {
        lastProjectionWidth = width;
        lastProjectionHeight = height;
        lastProjectionMatrix = screenOrthoMatrix(width, height);
        return lastProjectionMatrix;
    }

    /** Draws all visible sprites in {@code scene} as filled rects (or records them when headless). */
    public void render(Scene scene) {
        lastDrawCommands = collectDrawCommands(scene);
        if (shapes == null || lastDrawCommands.isEmpty()) {
            return;
        }
        shapes.setProjectionMatrix(
                prepareScreenProjection(screenWidth.getAsInt(), screenHeight.getAsInt()));
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (ShapeDrawCommand draw : lastDrawCommands) {
            shapes.setColor(draw.r, draw.g, draw.b, draw.a);
            shapes.rect(draw.x, draw.y, draw.width, draw.height);
        }
        shapes.end();
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
        if (shapes != null) {
            shapes.dispose();
        }
    }
}
