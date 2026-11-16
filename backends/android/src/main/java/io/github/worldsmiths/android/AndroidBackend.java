package io.github.worldsmiths.android;

import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Entry helper for the Android backend.
 *
 * <p>Mirrors desktop {@code DesktopBackend.start} ownership without constructing an Activity in
 * this jar: the generated app supplies an {@link AndroidApplicationHost} (typically wrapping
 * {@code AndroidApplication#initialize}).
 */
public final class AndroidBackend {

    private AndroidBackend() {}

    /**
     * Attaches a {@link WorldsmithsGame} to an existing Android application host with the given
     * config. Does not start an Activity; the host owns lifecycle entry (e.g. {@code onCreate}).
     */
    public static void attach(
            AndroidApplicationHost application, WorldsmithsGame game, AndroidConfig config) {
        if (application == null) {
            throw new NullPointerException("application");
        }
        if (game == null) {
            throw new NullPointerException("game");
        }
        if (config == null) {
            throw new NullPointerException("config");
        }

        application.initialize(new AndroidGameListener(game), config);
    }
}
