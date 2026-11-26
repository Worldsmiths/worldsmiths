package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.Audio;
import io.github.worldsmiths.core.Clock;
import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Files;
import io.github.worldsmiths.core.Graphics;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Log;
import io.github.worldsmiths.core.Textures;
import io.github.worldsmiths.core.Ui;
import io.github.worldsmiths.core.thin3d.Thin3D;
import io.github.worldsmiths.core.ui.DefaultUiKit;
import io.github.worldsmiths.core.ui.UiKit;

/**
 * Factory for a GDX-backed {@link EngineServices} bag. Backends call {@link #create()} after the
 * LibGDX application is live. Games should not import this package — use the bag passed to
 * {@link io.github.worldsmiths.core.WorldsmithsGame#create(EngineServices)}.
 */
public final class GdxEngineServices {
    private GdxEngineServices() {}

    /**
     * Assembles non-null Input/Clock/Graphics/Draw2D/Files/Thin3D/Audio/Ui/Textures/Log wrappers
     * around {@code Gdx} statics. Sets Application log level to {@link Application#LOG_DEBUG} so
     * author {@code debug} calls are visible on every backend.
     */
    public static EngineServices create() {
        return new Bag(
                new GdxInput(),
                new GdxClock(),
                new GdxGraphics(),
                new GdxDraw2D(),
                new GdxFiles(),
                new GdxThin3D(),
                new GdxAudio(),
                new GdxUi(),
                new GdxTextures(),
                createLog(gdxAppLogging()),
                new DefaultUiKit(),
                new EngineServices.Extensions());
    }

    /**
     * Sets {@link Application#LOG_DEBUG} on {@code appLogging} and returns a {@link GdxLog} over
     * its sink. Package-visible headless seam: {@link #create()} calls this before bag assembly;
     * unit tests inject a recording {@link AppLogging} without a live Application / GL (full {@link
     * #create()} still needs GL for Draw2D).
     */
    static GdxLog createLog(AppLogging appLogging) {
        appLogging.setLogLevel(Application.LOG_DEBUG);
        return new GdxLog(appLogging.sink());
    }

    private static AppLogging gdxAppLogging() {
        return new AppLogging() {
            @Override
            public void setLogLevel(int logLevel) {
                Gdx.app.setLogLevel(logLevel);
            }

            @Override
            public GdxLog.Sink sink() {
                return new GdxLog.Sink() {
                    @Override
                    public void debug(String tag, String message) {
                        Gdx.app.debug(tag, message);
                    }

                    @Override
                    public void log(String tag, String message) {
                        Gdx.app.log(tag, message);
                    }

                    @Override
                    public void error(String tag, String message) {
                        Gdx.app.error(tag, message);
                    }

                    @Override
                    public void error(String tag, String message, Throwable exception) {
                        Gdx.app.error(tag, message, exception);
                    }
                };
            }
        };
    }

    /**
     * Narrow Application logging seam: {@link #setLogLevel(int)} plus a {@link GdxLog.Sink}.
     * Production {@link #create()} wraps {@code Gdx.app}; unit tests inject a recording stub via
     * {@link #createLog(AppLogging)}.
     */
    interface AppLogging {
        void setLogLevel(int logLevel);

        GdxLog.Sink sink();
    }

    /**
     * Package-visible unwrap of the GDX texture cache from a bag created by {@link #create()}. Used
     * by retained-scene draw so backends share the same preload/resolve cache as games.
     */
    static GdxTextures requireGdxTextures(EngineServices services) {
        if (!(services instanceof Bag)) {
            throw new IllegalArgumentException(
                    "EngineServices must be created by GdxEngineServices.create()");
        }
        return ((Bag) services).gdxTextures;
    }

    /**
     * Disposes the engine-owned texture cache when {@code services} was assembled by {@link
     * #create()}. No-op for Fake / injected bags. Call from backend listener teardown after the
     * scene renderer shuts down.
     */
    public static void disposeTextures(EngineServices services) {
        if (services instanceof Bag) {
            ((Bag) services).gdxTextures.dispose();
        }
    }

    private static final class Bag implements EngineServices {
        private final Input input;
        private final Clock clock;
        private final Graphics graphics;
        private final Draw2D draw2d;
        private final Files files;
        private final Thin3D thin3d;
        private final Audio audio;
        private final Ui ui;
        private final GdxTextures gdxTextures;
        private final Log log;
        private final UiKit uiKit;
        private final Extensions extensions;

        Bag(
                Input input,
                Clock clock,
                Graphics graphics,
                Draw2D draw2d,
                Files files,
                Thin3D thin3d,
                Audio audio,
                Ui ui,
                GdxTextures gdxTextures,
                Log log,
                UiKit uiKit,
                Extensions extensions) {
            this.input = input;
            this.clock = clock;
            this.graphics = graphics;
            this.draw2d = draw2d;
            this.files = files;
            this.thin3d = thin3d;
            this.audio = audio;
            this.ui = ui;
            this.gdxTextures = gdxTextures;
            this.log = log;
            this.uiKit = uiKit;
            this.extensions = extensions;
        }

        @Override
        public Input input() {
            return input;
        }

        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public Graphics graphics() {
            return graphics;
        }

        @Override
        public Draw2D draw2d() {
            return draw2d;
        }

        @Override
        public Files files() {
            return files;
        }

        @Override
        public Thin3D thin3d() {
            return thin3d;
        }

        @Override
        public Audio audio() {
            return audio;
        }

        @Override
        public Ui ui() {
            return ui;
        }

        @Override
        public Textures textures() {
            return gdxTextures;
        }

        @Override
        public UiKit uiKit() {
            return uiKit;
        }

        @Override
        public Log log() {
            return log;
        }

        @Override
        public Extensions extensions() {
            return extensions;
        }
    }
}
