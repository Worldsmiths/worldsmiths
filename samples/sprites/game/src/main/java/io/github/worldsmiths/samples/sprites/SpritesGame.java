package io.github.worldsmiths.samples.sprites;

import io.github.worldsmiths.core.ComponentRegistries;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneCodec;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.WorldsmithsGame;
import java.util.HashSet;
import java.util.Set;

/**
 * Packaged textured-sprite sample: loads {@code scenes/demo.json} via {@link
 * EngineServices#files()} + {@link SceneCodec}, sync-preloads distinct texture keys, and retains
 * the scene for backend draw. Game sources stay free of LibGDX types.
 */
public final class SpritesGame implements WorldsmithsGame {

    static final String SCENE_PATH = "scenes/demo.json";

    private Scene scene;

    @Override
    public Scene createScene() {
        // Placeholder retained by the backend; populated in {@link #create} once Files is available.
        scene = new Scene("sprites");
        return scene;
    }

    @Override
    public void create(EngineServices services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        String json = services.files().readString(SCENE_PATH);
        Scene loaded = SceneCodec.create(ComponentRegistries.builtins()).fromJson(json);
        adoptLoadedScene(loaded);
        preloadDistinctTextureKeys(services, scene);
        services.graphics().setTitle("Sprites");
    }

    @Override
    public void update(float delta) {
        // Static demo scene — no per-frame logic.
    }

    @Override
    public void render() {
        // Retained scene is drawn by the backend SceneShapeRenderer.
    }

    /** Package-visible for headless tests that assert the retained scene identity. */
    Scene retainedScene() {
        return scene;
    }

    private void adoptLoadedScene(Scene loaded) {
        scene.name = loaded.name;
        scene.nodes.clear();
        for (int i = 0; i < loaded.nodes.size(); i++) {
            scene.nodes.add(loaded.nodes.get(i));
        }
    }

    private static void preloadDistinctTextureKeys(EngineServices services, Scene scene) {
        Set<String> keys = new HashSet<String>();
        collectTextureKeys(scene.nodes, keys);
        for (String key : keys) {
            services.textures().preload(key);
        }
    }

    private static void collectTextureKeys(Iterable<Node> nodes, Set<String> keys) {
        for (Node node : nodes) {
            Sprite sprite = node.get(Sprite.class);
            if (sprite != null && sprite.texture != null && !sprite.texture.trim().isEmpty()) {
                keys.add(sprite.texture);
            }
            collectTextureKeys(node.children, keys);
        }
    }
}
