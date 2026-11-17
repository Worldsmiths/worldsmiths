package io.github.worldsmiths.core;

/**
 * Always-non-null services bag for unit tests and headless game harnesses. Exposes typed fakes so
 * tests can set key/pointer/clock/graphics state without starting a display.
 */
public final class FakeEngineServices implements EngineServices {
    private final FakeInput input = new FakeInput();
    private final FakeClock clock = new FakeClock();
    private final FakeGraphics graphics = new FakeGraphics();
    private final Extensions extensions = new Extensions();

    @Override
    public FakeInput input() {
        return input;
    }

    @Override
    public FakeClock clock() {
        return clock;
    }

    @Override
    public FakeGraphics graphics() {
        return graphics;
    }

    @Override
    public Extensions extensions() {
        return extensions;
    }
}
