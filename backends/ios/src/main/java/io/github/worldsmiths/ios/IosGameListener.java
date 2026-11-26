package io.github.worldsmiths.ios;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;
import io.github.worldsmiths.core.gdx.GdxEngineServices;

/**
 * LibGDX {@link ApplicationListener} that drives a {@link WorldsmithsGame}, clears the framebuffer
 * each frame, and draws visible scene sprites as filled rects via {@link SceneShapeRenderer}.
 */
public final class IosGameListener implements ApplicationListener {

    /**
     * Color + depth clear each frame. Package-visible so unit tests can assert the depth bit without
     * a real GL context (same spirit as {@link SceneShapeRenderer#recordingOnly()}).
     */
    static final int FRAME_CLEAR_MASK = GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT;

    private final WorldsmithsGame game;
    /** Non-null only when tests inject a headless bag; production leaves this null. */
    private final EngineServices engineServicesOverride;
    private EngineServices services;
    private Scene scene;
    private SceneShapeRenderer sceneRenderer;

    public IosGameListener(WorldsmithsGame game) {
        this(game, null, null);
    }

    /**
     * Package-visible constructor so unit tests can inject a recording {@link SceneShapeRenderer}
     * without constructing a real libGDX {@code ShapeRenderer} (no RoboVM / GL).
     */
    IosGameListener(WorldsmithsGame game, SceneShapeRenderer sceneRenderer) {
        this(game, sceneRenderer, null);
    }

    /**
     * Package-visible test seam: inject {@link EngineServices} so headless tests can call
     * {@link #create()} without assembling a GDX-backed bag (no RoboVM / GL).
     */
    IosGameListener(
            WorldsmithsGame game, SceneShapeRenderer sceneRenderer, EngineServices engineServices) {
        if (game == null) {
            throw new NullPointerException("game");
        }
        this.game = game;
        this.sceneRenderer = sceneRenderer;
        this.engineServicesOverride = engineServices;
    }

    @Override
    public void create() {
        scene = game.createScene();
        // Backends assemble the portable bag after Application start (GDX live).
        services =
                engineServicesOverride != null ? engineServicesOverride : GdxEngineServices.create();
        game.create(services);
    }

    /**
     * Scene retained from {@link WorldsmithsGame#createScene()}. Package-visible for unit tests.
     */
    Scene retainedScene() {
        return scene;
    }

    @Override
    public void resize(int width, int height) {
        // No-op until Scene / viewport work in later milestones.
    }

    @Override
    public void render() {
        clearScreen();
        ensureSceneRenderer();
        updateAndRender(Gdx.graphics.getDeltaTime());
    }

    /**
     * Forwards one update/render tick to the game, then draws the retained scene. Package-visible
     * so unit tests can exercise the lifecycle without starting RoboVM.
     */
    void updateAndRender(float delta) {
        game.update(delta);
        game.render();
        if (sceneRenderer != null && scene != null) {
            sceneRenderer.render(scene);
        }
    }

    private void ensureSceneRenderer() {
        if (sceneRenderer == null) {
            sceneRenderer = new SceneShapeRenderer();
        }
    }

    @Override
    public void pause() {
        // No-op for D7.
    }

    @Override
    public void resume() {
        // No-op for D7.
    }

    @Override
    public void dispose() {
        game.dispose();
        if (sceneRenderer != null) {
            sceneRenderer.dispose();
        }
        GdxEngineServices.disposeTextures(services);
    }

    private static void clearScreen() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(FRAME_CLEAR_MASK);
    }
}
