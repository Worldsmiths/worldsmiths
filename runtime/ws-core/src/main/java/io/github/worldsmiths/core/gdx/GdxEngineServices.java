package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.Actions;
import io.github.worldsmiths.core.Assets;
import io.github.worldsmiths.core.Audio;
import io.github.worldsmiths.core.Clock;
import io.github.worldsmiths.core.DefaultActions;
import io.github.worldsmiths.core.DefaultScenes;
import io.github.worldsmiths.core.DeviceState;
import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.FakeAssets;
import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.Files;
import io.github.worldsmiths.core.Graphics;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Log;
import io.github.worldsmiths.core.Scenes;
import io.github.worldsmiths.core.Textures;
import io.github.worldsmiths.core.Ui;
import io.github.worldsmiths.core.thin3d.Thin3D;
import io.github.worldsmiths.core.ui.DefaultUiKit;
import io.github.worldsmiths.core.ui.UiKit;

/**
 * Factory for a GDX-backed {@link EngineServices} bag with a TeaVM-safe cooperative assets path.
 * Web calls {@link #createCooperative()}. Desktop / Android / iOS use the separate JVM factory in
 * this package so the threaded loader stack stays off the web-reachable call graph. Games should
 * not import this package — use the bag passed to {@link
 * io.github.worldsmiths.core.WorldsmithsGame#create(EngineServices)}.
 */
public final class GdxEngineServices {
    private GdxEngineServices() {}

    /**
     * Assembles non-null Input/Clock/Graphics/Draw2D/Files/Thin3D/Audio/Ui/Textures/Log/Assets/Scenes
     * wrappers around {@code Gdx} statics with cooperative (≤1 open/pump) async assets. Sets
     * Application log level to {@link Application#LOG_DEBUG} so author {@code debug} calls are
     * visible on every backend. Used by the web listener.
     */
    public static EngineServices createCooperative() {
        GdxTextures textures = new GdxTextures();
        return bag(textures, new GdxAssets(textures));
    }

    /**
     * Shared bag wiring for cooperative and JVM factories. Package-visible so the JVM-only factory
     * can assemble an identical bag with a different assets backend.
     */
    static EngineServices bag(GdxTextures textures, GdxPumpableAssets assets) {
        GdxFiles files = new GdxFiles();
        DeviceState devices = new DeviceState();
        GdxActionDevices actionDevices = new GdxActionDevices(devices, defaultGamepadSource());
        return new Bag(
                new GdxInput(devices),
                new DefaultActions(devices),
                actionDevices,
                new GdxClock(),
                new GdxGraphics(),
                new GdxDraw2D(),
                files,
                new GdxThin3D(),
                new GdxAudio(),
                new GdxUi(),
                textures,
                assets,
                createLog(gdxAppLogging()),
                new DefaultUiKit(),
                new DefaultScenes(files, textures),
                new EngineServices.Extensions());
    }

    /**
     * iOS stays soft-absent via {@link EmptyGamepadSource}: no {@code gdx-controllers-ios}, no
     * {@code IosControllerManager} forceLink, and no hardware pad AC in ticket 04. Desktop / web /
     * Android poll via {@link ControllersGamepadSource}.
     */
    static GamepadSource defaultGamepadSource() {
        if (Gdx.app != null && Gdx.app.getType() == ApplicationType.iOS) {
            return EmptyGamepadSource.INSTANCE;
        }
        return new ControllersGamepadSource();
    }

    /**
     * Package-visible headless seam: cooperative assets instance (no Application / GL).
     */
    static Assets newCooperativeAssetsForTest(GdxTextures textures) {
        return new GdxAssets(textures);
    }

    /**
     * Advances in-flight async loads for a bag from {@link #createCooperative()} or the JVM
     * factory, or a {@link FakeAssets} when tests inject {@link FakeEngineServices}. Also syncs
     * GDX devices into {@link DeviceState} and ticks {@link Actions} edges when the bag was
     * assembled by a GDX factory. No-op for unknown bags. Call once per frame before {@code
     * game.update}.
     */
    public static void pumpAssets(EngineServices services) {
        if (services instanceof Bag) {
            Bag bag = (Bag) services;
            bag.gdxAssets.pump();
            if (bag.actionDevices != null && bag.actions != null) {
                bag.actionDevices.sync();
                bag.actions.tick();
            }
            return;
        }
        if (services instanceof FakeEngineServices) {
            ((FakeEngineServices) services).assets().pump();
        } else if (services != null) {
            Assets assets = services.assets();
            if (assets instanceof FakeAssets) {
                ((FakeAssets) assets).pump();
            }
        }
    }

    /**
     * Sets {@link Application#LOG_DEBUG} on {@code appLogging} and returns a {@link GdxLog} over
     * its sink. Package-visible headless seam: factories call this before bag assembly; unit tests
     * inject a recording {@link AppLogging} without a live Application / GL (full bag create still
     * needs GL for Draw2D).
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
     * Production factories wrap {@code Gdx.app}; unit tests inject a recording stub via {@link
     * #createLog(AppLogging)}.
     */
    interface AppLogging {
        void setLogLevel(int logLevel);

        GdxLog.Sink sink();
    }

    /**
     * Package-visible unwrap of the GDX texture cache from a bag created by a GDX factory. Used by
     * retained-scene draw so backends share the same preload/resolve cache as games.
     */
    static GdxTextures requireGdxTextures(EngineServices services) {
        if (!(services instanceof Bag)) {
            throw new IllegalArgumentException(
                    "EngineServices must be created by a GdxEngineServices factory");
        }
        return ((Bag) services).gdxTextures;
    }

    /**
     * Package-visible headless bag for {@link GdxAssets} / pump / dispose tests without a live
     * Application / GL context. Only {@link EngineServices#textures()}, {@link
     * EngineServices#assets()}, and dispose/pump helpers are valid.
     */
    static EngineServices bagForAssetsTest(GdxTextures textures, GdxPumpableAssets assets) {
        return new Bag(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                textures,
                assets,
                null,
                null,
                null,
                new EngineServices.Extensions());
    }

    /**
     * Disposes in-flight async loads then the engine-owned texture cache when {@code services} was
     * assembled by a GDX factory. No-op for Fake / injected bags. Call from backend listener
     * teardown after the scene renderer shuts down.
     */
    public static void disposeTextures(EngineServices services) {
        if (services instanceof Bag) {
            ((Bag) services).gdxAssets.dispose();
            ((Bag) services).gdxTextures.dispose();
        }
    }

    private static final class Bag implements EngineServices {
        private final Input input;
        private final Actions actions;
        private final GdxActionDevices actionDevices;
        private final Clock clock;
        private final Graphics graphics;
        private final Draw2D draw2d;
        private final Files files;
        private final Thin3D thin3d;
        private final Audio audio;
        private final Ui ui;
        private final GdxTextures gdxTextures;
        private final GdxPumpableAssets gdxAssets;
        private final Log log;
        private final UiKit uiKit;
        private final Scenes scenes;
        private final Extensions extensions;

        Bag(
                Input input,
                Actions actions,
                GdxActionDevices actionDevices,
                Clock clock,
                Graphics graphics,
                Draw2D draw2d,
                Files files,
                Thin3D thin3d,
                Audio audio,
                Ui ui,
                GdxTextures gdxTextures,
                GdxPumpableAssets gdxAssets,
                Log log,
                UiKit uiKit,
                Scenes scenes,
                Extensions extensions) {
            this.input = input;
            this.actions = actions;
            this.actionDevices = actionDevices;
            this.clock = clock;
            this.graphics = graphics;
            this.draw2d = draw2d;
            this.files = files;
            this.thin3d = thin3d;
            this.audio = audio;
            this.ui = ui;
            this.gdxTextures = gdxTextures;
            this.gdxAssets = gdxAssets;
            this.log = log;
            this.uiKit = uiKit;
            this.scenes = scenes;
            this.extensions = extensions;
        }

        @Override
        public Input input() {
            return input;
        }

        @Override
        public Actions actions() {
            return actions;
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
        public Assets assets() {
            return gdxAssets;
        }

        @Override
        public Scenes scenes() {
            return scenes;
        }

        @Override
        public Extensions extensions() {
            return extensions;
        }
    }
}
