package io.github.worldsmiths.web;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Entry point for the TeaVM / gdx-teavm web application.
 *
 * <p>{@link WebConfig} carries canvas width/height and {@code storagePrefix}. Page title comes from
 * gdx-teavm {@code htmlTitle} (plugin DSL {@code targets.web { title }}) and optionally {@code
 * Gdx.graphics.setTitle} in game code — not from {@link WebConfig}.
 */
public final class WebBackend {

    private WebBackend() {}

    /**
     * Starts a web application that drives {@code game} with the given window config. Does not
     * return while the page hosts the canvas.
     */
    public static void start(WorldsmithsGame game, WebConfig config) {
        if (game == null) {
            throw new NullPointerException("game");
        }
        if (config == null) {
            throw new NullPointerException("config");
        }

        new WebApplication(new WebGameListener(game), toWebConfig(config));
    }

    /** Maps {@link WebConfig} onto a TeaVM web configuration (package-visible for tests). */
    static WebApplicationConfiguration toWebConfig(WebConfig config) {
        WebApplicationConfiguration webConfig = new WebApplicationConfiguration();
        webConfig.width = config.getWidth();
        webConfig.height = config.getHeight();
        webConfig.storagePrefix = config.getStoragePrefix();
        return webConfig;
    }
}
