package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.Clock;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Graphics;
import io.github.worldsmiths.core.Input;

/**
 * Factory for a GDX-backed {@link EngineServices} bag. Backends call {@link #create()} after the
 * LibGDX application is live. Games should not import this package — use the bag passed to
 * {@link io.github.worldsmiths.core.WorldsmithsGame#create(EngineServices)}.
 */
public final class GdxEngineServices {
    private GdxEngineServices() {}

    /** Assembles non-null Input/Clock/Graphics wrappers around {@code Gdx} statics. */
    public static EngineServices create() {
        return new Bag(new GdxInput(), new GdxClock(), new GdxGraphics(), new EngineServices.Extensions());
    }

    private static final class Bag implements EngineServices {
        private final Input input;
        private final Clock clock;
        private final Graphics graphics;
        private final Extensions extensions;

        Bag(Input input, Clock clock, Graphics graphics, Extensions extensions) {
            this.input = input;
            this.clock = clock;
            this.graphics = graphics;
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
        public Extensions extensions() {
            return extensions;
        }
    }
}
