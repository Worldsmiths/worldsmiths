package io.github.worldsmiths.samples.sprites;

import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Packaged textured-sprite sample: loads {@code scenes/demo.json} via {@link
 * EngineServices#scenes()}, which sync-preloads distinct Sprite texture keys. Game sources stay free
 * of LibGDX types.
 */
public final class SpritesGame implements WorldsmithsGame {

    static final String SCENE_PATH = "scenes/demo.json";

    private Scene scene;

    @Override
    public void create(EngineServices services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        services.scenes().load(SCENE_PATH);
        scene = services.scenes().current();
        services.graphics().setTitle("Sprites");
        services.log().info("Sprites scene loaded (" + scene.nodes.size() + " nodes)");
        services.log().warn("Sprites demo: textured draw is backend-owned; no per-frame updates");
    }

    @Override
    public void update(float delta) {
        // Static demo scene — no per-frame logic.
    }

    @Override
    public void render() {
        // Retained scene is drawn by the backend SceneShapeRenderer.
    }

    /** Package-visible for headless tests that assert the current scene. */
    Scene retainedScene() {
        return scene;
    }
}
