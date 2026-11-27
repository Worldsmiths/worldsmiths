package io.github.worldsmiths.core;

import io.github.worldsmiths.core.thin3d.FakeThin3D;
import io.github.worldsmiths.core.ui.FakeUiKit;

/**
 * Always-non-null services bag for unit tests and headless game harnesses. Exposes typed fakes so
 * tests can set key/pointer/actions/clock/graphics/files/audio/ui/textures/log/assets/scenes state
 * and assert Draw2D / Thin3D / Audio / Ui / Textures / Log / Assets / Scenes recordings without a
 * display.
 */
public final class FakeEngineServices implements EngineServices {
    /** Shared held/edge port for {@link #input()} keys and {@link #actions()} Control polls. */
    private final DeviceState devices = new DeviceState();
    private final FakeInput input = new FakeInput(devices);
    private final FakeActions actions = new FakeActions(devices);
    private final FakeClock clock = new FakeClock();
    private final FakeGraphics graphics = new FakeGraphics();
    private final FakeDraw2D draw2d = new FakeDraw2D();
    private final FakeFiles files = new FakeFiles();
    private final FakeThin3D thin3d = new FakeThin3D();
    private final FakeAudio audio = new FakeAudio();
    private final FakeUi ui = new FakeUi();
    private final FakeTextures textures = new FakeTextures();
    private final FakeUiKit uiKit = new FakeUiKit();
    private final FakeLog log = new FakeLog();
    /** Shares {@link #textures} so async TEXTURE COMPLETE merges into the sync cache (AC4). */
    private final FakeAssets assets = new FakeAssets(textures);
    /** Shares {@link #files} and {@link #textures} for path load + Sprite key preload. */
    private final FakeScenes scenes = new FakeScenes(files, textures);
    private final Extensions extensions = new Extensions();

    @Override
    public FakeInput input() {
        return input;
    }

    @Override
    public FakeActions actions() {
        return actions;
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
    public FakeTextures textures() {
        return textures;
    }

    @Override
    public FakeUiKit uiKit() {
        return uiKit;
    }

    @Override
    public FakeLog log() {
        return log;
    }

    @Override
    public FakeAssets assets() {
        return assets;
    }

    @Override
    public FakeScenes scenes() {
        return scenes;
    }

    @Override
    public Extensions extensions() {
        return extensions;
    }
}
