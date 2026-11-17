package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.List;

/**
 * GDX-free 2D scene walk: collects filled-rect draw commands from visible {@link Sprite}s with
 * {@link Transform2D} cell coordinates.
 */
public final class SceneWalk {

    /** Pixel size of one scene cell ({@link Transform2D} units map 1:1 to cells). */
    public static final float CELL_SIZE = 32f;

    private SceneWalk() {}

    /**
     * Collects filled-rect draw commands for all visible sprites in {@code scene}. Empty or null
     * scene yields an empty list. Tree order is preserved; invisible sprites and nodes missing
     * transform or sprite are skipped.
     */
    public static List<RectDraw> collectDrawCommands(Scene scene) {
        List<RectDraw> draws = new ArrayList<RectDraw>();
        if (scene == null) {
            return draws;
        }
        for (Node node : scene.nodes) {
            collectFromNode(node, draws);
        }
        return draws;
    }

    private static void collectFromNode(Node node, List<RectDraw> draws) {
        Transform2D transform = node.get(Transform2D.class);
        Sprite sprite = node.get(Sprite.class);
        if (transform != null && sprite != null && sprite.visible) {
            draws.add(
                    new RectDraw(
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
}
