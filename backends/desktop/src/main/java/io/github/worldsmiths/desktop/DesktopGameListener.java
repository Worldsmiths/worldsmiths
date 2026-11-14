package io.github.worldsmiths.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * LibGDX {@link ApplicationListener} that drives a {@link WorldsmithsGame} and clears the
 * framebuffer each frame. Retains the scene from {@link WorldsmithsGame#createScene()} for later
 * drawing (D2 shape renderer lands in a follow-up task).
 */
public final class DesktopGameListener implements ApplicationListener {

    private final WorldsmithsGame game;
    private Scene scene;

    public DesktopGameListener(WorldsmithsGame game) {
        if (game == null) {
            throw new NullPointerException("game");
        }
        this.game = game;
    }

    @Override
    public void create() {
        scene = game.createScene();
        game.create();
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
        updateAndRender(Gdx.graphics.getDeltaTime());
    }

    /**
     * Forwards one update/render tick to the game. Package-visible so unit tests can exercise
     * the lifecycle without starting LWJGL or requiring a display.
     */
    void updateAndRender(float delta) {
        game.update(delta);
        game.render();
    }

    @Override
    public void pause() {
        // No-op for D1.
    }

    @Override
    public void resume() {
        // No-op for D1.
    }

    @Override
    public void dispose() {
        // No-op for D1.
    }

    private static void clearScreen() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
