package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.List;

/**
 * GDX-free 2D scene walk: collects draw commands from visible {@link Sprite}s with {@link
 * Transform2D} cell coordinates. Non-blank {@link Sprite#texture} keys are copied onto each {@link
 * RectDraw#textureKey}; null/blank texture yields a colored-rect command ({@code textureKey}
 * null).
 */
public final class SceneWalk {

    /** Pixel size of one scene cell ({@link Transform2D} units map 1:1 to cells). */
    public static final float CELL_SIZE = 32f;

    private SceneWalk() {}

    /**
     * Collects draw commands for all visible sprites in {@code scene}. Empty or null scene yields
     * an empty list. Tree order is preserved; invisible sprites and nodes missing transform or
     * sprite are skipped. Each command carries optional {@link RectDraw#textureKey} and rgba tint.
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

    /** Non-blank texture keys are copied onto the draw command; null/blank become null. */
    private static String textureKeyOrNull(String texture) {
        if (texture == null || texture.trim().isEmpty()) {
            return null;
        }
        return texture;
    }

    private static void collectFromNode(Node node, List<RectDraw> draws) {
        Transform2D transform = node.get(Transform2D.class);
        Sprite sprite = node.get(Sprite.class);
        if (transform != null && sprite != null && sprite.visible) {
            String textureKey = textureKeyOrNull(sprite.texture);
            draws.add(
                    new RectDraw(
                            transform.x * CELL_SIZE,
                            transform.y * CELL_SIZE,
                            CELL_SIZE,
                            CELL_SIZE,
                            sprite.r,
                            sprite.g,
                            sprite.b,
                            sprite.a,
                            textureKey));
        }
        for (Node child : node.children) {
            collectFromNode(child, draws);
        }
    }
}
