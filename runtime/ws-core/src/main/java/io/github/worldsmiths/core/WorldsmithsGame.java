package io.github.worldsmiths.core;

/**
 * Public contract for a Worldsmiths {@code gameClass}. Game modules implement this interface;
 * backends drive the lifecycle without exposing platform types to game code.
 *
 * <p>Games build a root {@link Scene} via {@link #createScene()}; the backend retains that scene
 * and later draws from it. Games still receive {@link #create()} / {@link #update(float)} /
 * {@link #render()} for setup and per-frame logic.
 */
public interface WorldsmithsGame {

    /**
     * Builds the root scene tree for this game. Called once by the backend before {@link #create()}.
     *
     * @return the scene the engine should retain (never {@code null})
     */
    Scene createScene();

    /** Called once when the application is ready, after {@link #createScene()}. */
    void create();

    /**
     * Called each frame before {@link #render()}.
     *
     * @param delta time since the previous update, in seconds
     */
    void update(float delta);

    /** Called each frame to draw game-owned content (engine draws the retained scene separately). */
    void render();

    /**
     * Releases game-owned GPU / native resources. Platform listeners call this on shutdown before
     * disposing their own renderers. Default intent is empty — most games no-op; samples that
     * allocate Models / ModelBatch free them here.
     */
    void dispose();
}
