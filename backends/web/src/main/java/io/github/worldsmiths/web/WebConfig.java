package io.github.worldsmiths.web;

/**
 * Canvas size and browser storage prefix for the TeaVM web backend.
 *
 * <p>Page title is not part of this config: set it via the plugin {@code htmlTitle} (from {@code
 * targets.web { title }}) and/or {@code Gdx.graphics.setTitle} in game code.
 *
 * <p>{@code storagePrefix} matches gdx-teavm {@code WebApplicationConfiguration.storagePrefix}
 * (default {@code "app"}). It is applied at runtime via {@link WebBackend}, not via TeaVM {@code
 * webDefaults} (which has no storagePrefix property).
 */
public final class WebConfig {

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    /** Same default as gdx-teavm {@code WebApplicationConfiguration.storagePrefix}. */
    public static final String DEFAULT_STORAGE_PREFIX = "app";

    private final int width;
    private final int height;
    private final String storagePrefix;

    public WebConfig() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_STORAGE_PREFIX);
    }

    public WebConfig(int width, int height) {
        this(width, height, DEFAULT_STORAGE_PREFIX);
    }

    public WebConfig(int width, int height, String storagePrefix) {
        if (storagePrefix == null) {
            throw new NullPointerException("storagePrefix");
        }
        this.width = width;
        this.height = height;
        this.storagePrefix = storagePrefix;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getStoragePrefix() {
        return storagePrefix;
    }
}
