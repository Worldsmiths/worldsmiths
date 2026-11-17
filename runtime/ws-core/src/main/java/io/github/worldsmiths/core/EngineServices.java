package io.github.worldsmiths.core;

/**
 * Services bag passed to {@link WorldsmithsGame#create(EngineServices)}. Fixed getters cover the D9
 * floor systems; {@link #extensions()} is an unused stub reserved for plugins/ECS later.
 *
 * <p>Every getter returns a non-null instance once {@code create} runs. Tests supply
 * {@link FakeEngineServices}; backends assemble GDX-backed impls.
 */
public interface EngineServices {

    Input input();

    Clock clock();

    Graphics graphics();

    /** Unused in D9 — empty stub for a future plugin/ECS escape hatch. */
    Extensions extensions();

    /** Nested empty stub; no methods yet. */
    final class Extensions {
        public Extensions() {}
    }
}
