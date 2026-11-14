package io.github.worldsmiths.desktop;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Walks a retained {@link Scene} and draws each visible {@link Sprite} as a filled rect from
 * {@link Transform2D} cell coordinates. Owns a libGDX {@link ShapeRenderer}; dispose when the
 * listener shuts down.
 */
public final class SceneShapeRenderer {

    /** Pixel size of one scene cell (Transform2D units map 1:1 to cells). */
    public static final float CELL_SIZE = 32f;

    private final ShapeRenderer shapes;
    private List<ShapeDrawCommand> lastDrawCommands = Collections.emptyList();
    private boolean disposed;

    public SceneShapeRenderer() {
        this(new ShapeRenderer());
    }

    /**
     * Recording-only instance: walks the scene and stores draw commands without touching GL.
     * Package-visible test seam used by {@link DesktopGameListenerTest}.
     */
    static SceneShapeRenderer recordingOnly() {
        return new SceneShapeRenderer(null);
    }

    /** Package-visible for tests that inject a fake or already-constructed renderer. */
    SceneShapeRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    /**
     * Collects filled-rect draw commands for the scene without touching GL. Package-visible test
     * seam and shared walk used by {@link #render(Scene)}.
     */
    static List<ShapeDrawCommand> collectDrawCommands(Scene scene) {
        List<ShapeDrawCommand> draws = new ArrayList<ShapeDrawCommand>();
        if (scene == null) {
            return draws;
        }
        for (Node node : scene.nodes) {
            collectFromNode(node, draws);
        }
        return draws;
    }

    private static void collectFromNode(Node node, List<ShapeDrawCommand> draws) {
        Transform2D transform = node.get(Transform2D.class);
        Sprite sprite = node.get(Sprite.class);
        if (transform != null && sprite != null && sprite.visible) {
            draws.add(
                    new ShapeDrawCommand(
                            transform.x * CELL_SIZE,
                            transform.y * CELL_SIZE,
                            CELL_SIZE,
                            CELL_SIZE,
                            sprite.r,
                            sprite.g,
                            sprite.b,
                            sprite.a));
        }
        for (Node child : node.children) {
            collectFromNode(child, draws);
        }
    }

    /** Draws all visible sprites in {@code scene} as filled rects (or records them when headless). */
    public void render(Scene scene) {
        lastDrawCommands = collectDrawCommands(scene);
        if (shapes == null || lastDrawCommands.isEmpty()) {
            return;
        }
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
