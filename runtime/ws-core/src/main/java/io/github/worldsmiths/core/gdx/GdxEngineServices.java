package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.Clock;
import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Files;
import io.github.worldsmiths.core.Graphics;
import io.github.worldsmiths.core.Input;

/**
 * Factory for a GDX-backed {@link EngineServices} bag. Backends call {@link #create()} after the
 * LibGDX application is live. Games should not import this package — use the bag passed to
 * {@link io.github.worldsmiths.core.WorldsmithsGame#create(EngineServices)}.
 */
public final class GdxEngineServices {
    private GdxEngineServices() {}

    /** Assembles non-null Input/Clock/Graphics/Draw2D/Files wrappers around {@code Gdx} statics. */
    public static EngineServices create() {
        return new Bag(
                new GdxInput(),
                new GdxClock(),
                new GdxGraphics(),
                new GdxDraw2D(),
                new GdxFiles(),
                new EngineServices.Extensions());
    }

    private static final class Bag implements EngineServices {
        private final Input input;
        private final Clock clock;
        private final Graphics graphics;
        private final Draw2D draw2d;
        private final Files files;
        private final Extensions extensions;

        Bag(
                Input input,
                Clock clock,
                Graphics graphics,
                Draw2D draw2d,
                Files files,
                Extensions extensions) {
            this.input = input;
            this.clock = clock;
            this.graphics = graphics;
            this.draw2d = draw2d;
            this.files = files;
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
        public Extensions extensions() {
            return extensions;
        }
    }
}
