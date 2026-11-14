package io.github.worldsmiths.spike.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import io.github.worldsmiths.spike.game.generated.GeneratedComponentRegistry;
import io.github.worldsmiths.spike.runtime.Node;
import io.github.worldsmiths.spike.runtime.Scene;
import io.github.worldsmiths.spike.runtime.SceneJson;

/** The spike's scene: built in code, serialized, deserialized and checked. Runs unchanged on the JVM and TeaVM. */
public final class DemoScene {
    private DemoScene() {
    }

    public static Scene build() {
        Transform2D playerTransform = new Transform2D(12.5f, -3.25f);
        playerTransform.rotation = 90f;
        Sprite hero = new Sprite("hero.png", 2);
        hero.alpha = 0.75f;

        Sprite shadowSprite = new Sprite("shadow.png", 1);
        shadowSprite.alpha = 0.5f;
        shadowSprite.visible = false;

        Transform2D groundTransform = new Transform2D(0f, -10f);
        groundTransform.scaleX = 4f;

        return new Scene("S2 demo")
                .add(new Node("Player")
                        .add(playerTransform)
                        .add(hero)
                        .addChild(new Node("Shadow").add(new Transform2D(0f, -1f)).add(shadowSprite)))
                .add(new Node("Ground").add(groundTransform));
    }

    public static Json json() {
        return SceneJson.create(GeneratedComponentRegistry.INSTANCE);
    }

    public static RoundTrip roundTrip() {
        Json json = json();
        String text = json.toJson(build());
        Scene decoded = json.fromJson(Scene.class, text);
        return new RoundTrip(text, decoded, verify(decoded));
    }

    /** Compares a decoded scene against the values {@link #build()} wrote; returns one message per mismatch. */
    public static Array<String> verify(Scene scene) {
        Array<String> failures = new Array<String>();
        check(failures, "scene.name", "S2 demo", scene.name);
        check(failures, "scene.nodes", 2, scene.nodes.size);
        if (scene.nodes.size != 2) return failures;

        Node player = scene.nodes.get(0);
        check(failures, "Player.name", "Player", player.name);
        checkTransform(failures, "Player", player.get(Transform2D.class), 12.5f, -3.25f, 90f, 1f, 1f);
        checkSprite(failures, "Player", player.get(Sprite.class), "hero.png", 2, 0.75f, true);
        check(failures, "Player.children", 1, player.children.size);
        if (player.children.size == 1) {
            Node shadow = player.children.get(0);
            check(failures, "Shadow.name", "Shadow", shadow.name);
            checkTransform(failures, "Shadow", shadow.get(Transform2D.class), 0f, -1f, 0f, 1f, 1f);
            checkSprite(failures, "Shadow", shadow.get(Sprite.class), "shadow.png", 1, 0.5f, false);
        }

        Node ground = scene.nodes.get(1);
        check(failures, "Ground.name", "Ground", ground.name);
        check(failures, "Ground.components", 1, ground.components.size);
        checkTransform(failures, "Ground", ground.get(Transform2D.class), 0f, -10f, 0f, 4f, 1f);
        return failures;
    }

    private static void checkTransform(Array<String> failures, String node, Transform2D t,
                                       float x, float y, float rotation, float scaleX, float scaleY) {
        if (t == null) {
            failures.add(node + ": missing Transform2D");
            return;
        }
        check(failures, node + ".Transform2D.x", x, t.x);
        check(failures, node + ".Transform2D.y", y, t.y);
        check(failures, node + ".Transform2D.rotation", rotation, t.rotation);
        check(failures, node + ".Transform2D.scaleX", scaleX, t.scaleX);
        check(failures, node + ".Transform2D.scaleY", scaleY, t.scaleY);
    }

    private static void checkSprite(Array<String> failures, String node, Sprite s,
                                    String texture, int layer, float alpha, boolean visible) {
        if (s == null) {
            failures.add(node + ": missing Sprite");
            return;
        }
        check(failures, node + ".Sprite.texture", texture, s.texture);
        check(failures, node + ".Sprite.layer", layer, s.layer);
        check(failures, node + ".Sprite.alpha", alpha, s.alpha);
        check(failures, node + ".Sprite.visible", visible, s.visible);
    }

    private static void check(Array<String> failures, String what, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            failures.add(what + ": expected " + expected + " but was " + actual);
        }
    }

    public static String describe(Scene scene) {
        StringBuilder out = new StringBuilder();
        out.append("Scene ").append(scene.name).append('\n');
        for (Node node : scene.nodes) describe(out, node, 1);
        return out.toString();
    }

    private static void describe(StringBuilder out, Node node, int depth) {
        indent(out, depth);
        out.append(node.name).append('\n');
        for (Object component : node.components) {
            indent(out, depth + 1);
            if (component instanceof Transform2D) {
                Transform2D t = (Transform2D) component;
                out.append("Transform2D x=").append(t.x).append(" y=").append(t.y).append(" rotation=").append(t.rotation)
                        .append(" scale=").append(t.scaleX).append(',').append(t.scaleY);
            } else if (component instanceof Sprite) {
                Sprite s = (Sprite) component;
                out.append("Sprite texture=").append(s.texture).append(" layer=").append(s.layer)
                        .append(" alpha=").append(s.alpha).append(" visible=").append(s.visible);
            } else {
                out.append(component);
            }
            out.append('\n');
        }
        for (Node child : node.children) describe(out, child, depth + 1);
    }

    private static void indent(StringBuilder out, int depth) {
        for (int i = 0; i < depth; i++) out.append("  ");
    }

    public static final class RoundTrip {
        public final String json;
        public final Scene decoded;
        public final Array<String> failures;

        RoundTrip(String json, Scene decoded, Array<String> failures) {
            this.json = json;
            this.decoded = decoded;
            this.failures = failures;
        }

        public boolean passed() {
            return failures.size == 0;
        }
    }
}
