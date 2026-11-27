package io.github.worldsmiths.core;

/**
 * Public contract for a Worldsmiths {@code gameClass}. Game modules implement this interface;
 * backends drive the lifecycle without exposing platform types to game code.
 *
 * <p>Games receive {@link #create(EngineServices)} / {@link #update(float)} / {@link #render()} for
 * setup and per-frame logic. The first scene is established inside {@link #create(EngineServices)}
 * via {@link EngineServices#scenes()} ({@code load} or {@code switchTo}).
 */
public interface WorldsmithsGame {

    /**
     * Called once when the application is ready. Games load or switch to their first scene through
     * {@code services.scenes()} during this call.
     *
     * @param services non-null engine services bag (backends assemble real impls; tests pass
     *     {@link FakeEngineServices})
     */
    void create(EngineServices services);

    /**
     * Called each frame before {@link #render()}.
     *
     * @param delta time since the previous update, in seconds
     */
    void update(float delta);

    /** Called each frame to draw game-owned content (engine draws {@code scenes().current()} separately). */
    void render();

    /**
     * Releases game-owned GPU / native resources. Platform listeners call this on shutdown before
     * disposing their own renderers. Default is empty — most games no-op; samples that allocate
     * Models / ModelBatch override and free them here.
     */
    default void dispose() {}
}
