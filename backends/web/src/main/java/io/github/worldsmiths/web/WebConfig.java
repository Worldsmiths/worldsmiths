package io.github.worldsmiths.web;

/**
 * Canvas size for the TeaVM web backend.
 *
 * <p>Page title is not part of this config: set it via the plugin {@code htmlTitle} (from {@code
 * targets.web { title }}) and/or {@code Gdx.graphics.setTitle} in game code.
 */
public final class WebConfig {

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    private final int width;
    private final int height;

    public WebConfig() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public WebConfig(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
