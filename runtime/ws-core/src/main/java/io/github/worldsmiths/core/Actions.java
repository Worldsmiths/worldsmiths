package io.github.worldsmiths.core;

/**
 * Named digital action map: bind controls, poll {@link #isDown(String)} / {@link
 * #justPressed(String)}, and advance frame edges with {@link #tick()}. Unknown or unbound polls are
 * soft-false; blank/null mutate throws {@link EngineException}.
 *
 * <p>Optional {@link #load(Files, String)} reads a tiny line-oriented packaged table (see method
 * javadoc). Missing path fails closed before any map mutation.
 */
public interface Actions {

    /**
     * Replace-all bindings for {@code action}. Any previous controls for the name are dropped.
     *
     * @throws EngineException if {@code action} is null/blank or any control is null
     */
    void bind(String action, Control... controls);

    /**
     * Replace-all bindings for each action listed in a packaged internal file.
     *
     * <p>Format (UTF-8, one binding per line): {@code action kind TOKEN}. {@code kind} is {@code
     * key}, {@code pad}, or {@code pointer}; {@code TOKEN} is a {@link Key}, {@link PadButton}, or
     * {@link PointerButton} name. Blank lines and {@code #} comments are ignored. Multiple lines for
     * the same action OR together (replace-all for that name). Actions not listed are left
     * unchanged.
     *
     * @throws EngineException if {@code files}/{@code path} is null/blank, the path is missing, or a
     *     line is malformed
     */
    void load(Files files, String path);

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
