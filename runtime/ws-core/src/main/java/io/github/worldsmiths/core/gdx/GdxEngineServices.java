package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.Audio;
import io.github.worldsmiths.core.Clock;
import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Files;
import io.github.worldsmiths.core.Graphics;
import io.github.worldsmiths.core.Input;
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
     * Assembles non-null Input/Clock/Graphics/Draw2D/Files/Thin3D/Audio/Ui/Textures wrappers around
     * {@code Gdx} statics.
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
                new DefaultUiKit(),
                new EngineServices.Extensions());
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
        public Extensions extensions() {
            return extensions;
        }
    }
}
