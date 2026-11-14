package io.github.worldsmiths.core;

/**
 * Public contract for a Worldsmiths {@code gameClass}. Game modules implement this interface;
 * backends drive the lifecycle without exposing platform types to game code.
 *
 * <p>Scene entry (for example {@code createScene()}) lands in D2. This D1 surface is the
 * minimal create / update / render loop only — no Scene API and no libGDX types.
 */
public interface WorldsmithsGame {

    /** Called once when the application is ready. */
    void create();

    /**
     * Called each frame before {@link #render()}.
     *
     * @param delta time since the previous update, in seconds
     */
    void update(float delta);

    /** Called each frame to draw. */
    void render();
}
