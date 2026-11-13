package io.github.worldsmiths.spike.ios;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

public class HelloGame extends ApplicationAdapter {
    private final int exitAfterFrames;
    private int frames;

    public HelloGame() {
        this(0);
    }

    /** With {@code exitAfterFrames > 0}, prints a marker line and exits after that many frames (CI smoke test). */
    public HelloGame(int exitAfterFrames) {
        this.exitAfterFrames = exitAfterFrames;
    }

    @Override
    public void create() {
        Gdx.app.log("S3", "hello from libGDX " + com.badlogic.gdx.Version.VERSION);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.2f, 0.35f, 1f);
        frames++;
        if (exitAfterFrames > 0 && frames == 1) {
            System.out.println("S3SMOKE first frame");
            System.out.flush();
        }
        if (exitAfterFrames > 0 && frames == exitAfterFrames) {
            System.out.println("S3SMOKE rendered " + frames + " frames, GL " + Gdx.gl.glGetString(com.badlogic.gdx.graphics.GL20.GL_VERSION));
            System.out.flush();
            System.exit(0);
        }
    }
}
