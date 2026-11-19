package io.github.worldsmiths.core;

import io.github.worldsmiths.core.thin3d.Thin3D;

/**
 * Services bag passed to {@link WorldsmithsGame#create(EngineServices)}. Fixed getters cover the D9
 * floor systems plus jam audio; {@link #extensions()} is an unused stub reserved for plugins/ECS
 * later.
 *
 * <p>Every getter returns a non-null instance once {@code create} runs. Tests supply
 * {@link FakeEngineServices}; backends assemble GDX-backed impls.
 */
public interface EngineServices {

    Input input();

    Clock clock();

    Graphics graphics();

    /** Immediate-mode 2D overlay drawing (lines/rects). */
    Draw2D draw2d();

    /** Sync read of packaged internal text/bytes. */
    Files files();

    /** Thin 3D: camera, lights, primitive meshes, batch begin/render/end. */
    Thin3D thin3d();

    /** One-shot sound load/play from packaged internal paths. */
    Audio audio();

    /** Unused in D9 — empty stub for a future plugin/ECS escape hatch. */
    Extensions extensions();

    /** Nested empty stub; no methods yet. */
    final class Extensions {
        public Extensions() {}
    }
}
