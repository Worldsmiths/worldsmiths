package io.github.worldsmiths.core;

import io.github.worldsmiths.core.d3.D3;
import io.github.worldsmiths.core.ui.UiKit;

/**
 * Services bag passed to {@link WorldsmithsGame#create(EngineServices)}. Fixed getters cover the D9
 * floor systems plus jam audio/ui, async {@link #assets()}, and replace-only {@link #scenes()};
 * {@link #extensions()} is an unused stub reserved for plugins/ECS later.
 *
 * <p>Every getter returns a non-null instance once {@code create} runs. Tests supply
 * {@link FakeEngineServices}; backends assemble GDX-backed impls.
 */
public interface EngineServices {

    Input input();

    /**
     * Named digital action map ({@link Actions#bind} / {@link Actions#isDown} / {@link
     * Actions#justPressed}). Fake bags wire {@link FakeActions}; the GDX bag wires a {@link
     * DefaultActions} over a shared {@link DeviceState}. Each frame the GDX bag syncs devices into
     * that port and ticks {@link Actions} edges (via {@code GdxEngineServices.pumpAssets}).
     */
    Actions actions();

    Clock clock();

    Graphics graphics();

    /** Immediate-mode 2D overlay drawing (lines/rects). */
    Draw2D draw2d();

    /** Sync read of packaged internal text/bytes. */
    Files files();

    /** Thin 3D: camera, lights, primitive meshes, batch begin/render/end. */
    D3 d3();

    /** One-shot sound load/play from packaged internal paths. */
    Audio audio();

    /** Bitmap font load/draw from packaged internal paths. */
    Ui ui();

    /** Sync texture load/cache from packaged internal paths. */
    Textures textures();

    /** Session widget toolkit (layout, hit, focus, draw). */
    UiKit uiKit();

    /**
     * Logging facade ({@code debug}/{@code info}/{@code warn}/{@code error}). Fake bags always wire
     * a recording {@link FakeLog}; the GDX bag wires a platform-backed impl when assembled.
     */
    Log log();

    /**
     * Async packaged-asset load facade ({@link Assets#request}). Fake bags wire a recording {@link
     * FakeAssets}; the GDX bag wires a platform-backed impl when assembled.
     */
    Assets assets();

    /**
     * Replace-only scene flow ({@link Scenes#load} / {@link Scenes#switchTo} / {@link
     * Scenes#unload} / {@link Scenes#current}). Fake bags wire a recording {@link FakeScenes}; the
     * GDX bag wires a platform-backed impl when assembled.
     */
    Scenes scenes();

    /** Unused in D9 — empty stub for a future plugin/ECS escape hatch. */
    Extensions extensions();

    /** Nested empty stub; no methods yet. */
    final class Extensions {
        public Extensions() {}
    }
}
