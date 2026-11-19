package io.github.worldsmiths.core;

import io.github.worldsmiths.core.thin3d.FakeThin3D;

/**
 * Always-non-null services bag for unit tests and headless game harnesses. Exposes typed fakes so
 * tests can set key/pointer/clock/graphics/files/audio/ui state and assert Draw2D / Thin3D / Audio
 * / Ui recordings without a display.
 */
public final class FakeEngineServices implements EngineServices {
    private final FakeInput input = new FakeInput();
    private final FakeClock clock = new FakeClock();
    private final FakeGraphics graphics = new FakeGraphics();
    private final FakeDraw2D draw2d = new FakeDraw2D();
    private final FakeFiles files = new FakeFiles();
    private final FakeThin3D thin3d = new FakeThin3D();
    private final FakeAudio audio = new FakeAudio();
    private final FakeUi ui = new FakeUi();
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
    public FakeDraw2D draw2d() {
        return draw2d;
    }

    @Override
    public FakeFiles files() {
        return files;
    }

    @Override
    public FakeThin3D thin3d() {
        return thin3d;
    }

    @Override
    public FakeAudio audio() {
        return audio;
    }

    @Override
    public FakeUi ui() {
        return ui;
    }

    @Override
    public Extensions extensions() {
        return extensions;
    }
}
