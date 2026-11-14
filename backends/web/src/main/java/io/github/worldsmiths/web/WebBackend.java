package io.github.worldsmiths.web;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Entry point for the TeaVM / gdx-teavm web application.
 *
 * <p>{@link WebApplicationConfiguration} exposes width and height but not a window title; {@link
 * WebConfig#getTitle()} is retained for API parity with desktop and for generated {@code
 * htmlTitle} in later milestones.
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

        WebApplicationConfiguration webConfig = new WebApplicationConfiguration();
        webConfig.width = config.getWidth();
        webConfig.height = config.getHeight();

        new WebApplication(new WebGameListener(game), webConfig);
    }
}
