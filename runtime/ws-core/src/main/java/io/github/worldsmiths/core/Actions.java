package io.github.worldsmiths.core;

/**
 * Named digital action map: bind controls, poll {@link #isDown(String)} / {@link
 * #justPressed(String)}, and advance frame edges with {@link #tick()}. Unknown or unbound polls are
 * soft-false; blank/null mutate throws {@link EngineException}.
 */
public interface Actions {

    /**
     * Replace-all bindings for {@code action}. Any previous controls for the name are dropped.
     *
     * @throws EngineException if {@code action} is null/blank or any control is null
     */
    void bind(String action, Control... controls);

    /** Whether any bound control for {@code action} is currently held. Soft-false if unknown. */
    boolean isDown(String action);

    /**
     * Whether {@code action} transitioned from up to down on the most recent {@link #tick()}.
     * Soft-false if unknown.
     */
    boolean justPressed(String action);

    /**
     * Advances one frame: clears prior justPressed edges and samples current held state for new
     * edges.
     */
    void tick();
}
