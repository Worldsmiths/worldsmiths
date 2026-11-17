package io.github.worldsmiths.ios;

import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Entry helper for the iOS backend.
 *
 * <p>Mirrors Android {@code AndroidBackend.attach} ownership without constructing an {@code
 * IOSApplication} in this jar: the generated Delegate supplies an {@link IosApplicationHost}.
 */
public final class IosBackend {

    private IosBackend() {}

    /**
     * Attaches a {@link WorldsmithsGame} to an existing iOS application host with the given config.
     * Does not start RoboVM; the host owns lifecycle entry (e.g. {@code createApplication}).
     */
    public static void attach(
            IosApplicationHost application, WorldsmithsGame game, IosConfig config) {
        if (application == null) {
            throw new NullPointerException("application");
        }
        if (game == null) {
            throw new NullPointerException("game");
        }
        if (config == null) {
            throw new NullPointerException("config");
        }

        application.initialize(new IosGameListener(game), config);
    }
}
