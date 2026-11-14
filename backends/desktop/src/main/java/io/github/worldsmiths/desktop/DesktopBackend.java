package io.github.worldsmiths.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Entry point for the real LWJGL3 desktop window.
 *
 * <p>macOS note: when a Gradle {@code run} task (or similar) launches this process, it should set
 * {@code -XstartOnFirstThread}. That JVM arg is owned by the plugin run task in T3 and is not set
 * here.
 */
public final class DesktopBackend {

    private DesktopBackend() {}

    /**
     * Starts a desktop application that drives {@code game} with the given window config. Blocks
     * until the window closes.
     */
    public static void start(WorldsmithsGame game, DesktopConfig config) {
        if (game == null) {
            throw new NullPointerException("game");
        }
        if (config == null) {
            throw new NullPointerException("config");
        }

        new Lwjgl3Application(new DesktopGameListener(game), toLwjglConfig(config));
    }

    /** Maps {@link DesktopConfig} onto an LWJGL3 configuration (package-visible for tests). */
    static Lwjgl3ApplicationConfiguration toLwjglConfig(DesktopConfig config) {
        Lwjgl3ApplicationConfiguration lwjglConfig = new Lwjgl3ApplicationConfiguration();
        lwjglConfig.setTitle(config.getTitle());
        lwjglConfig.setWindowedMode(config.getWidth(), config.getHeight());
        lwjglConfig.useVsync(config.isVsync());
        lwjglConfig.setResizable(config.isResizable());
        if (config.getIcon() != null) {
            // Paths are relative to assetsDir (desktop run working directory / shipped assets root).
            lwjglConfig.setWindowIcon(config.getIcon());
        }
        return lwjglConfig;
    }
}
