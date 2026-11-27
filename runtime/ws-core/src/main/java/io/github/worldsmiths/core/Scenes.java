package io.github.worldsmiths.core;

/**
 * Replace-only scene flow facade from {@link EngineServices#scenes()}. Path load and {@link
 * #switchTo(Scene)} exit the previous current (if any), set {@link #current()}, then enter the next.
 * Failed path loads throw {@link EngineException} and keep the previous current.
 */
public interface Scenes {

    /**
     * Sets the component registry used by path {@link #load(String)} for {@link SceneCodec}. Call
     * before the first path load when not relying on the default builtins registry.
     */
    void setRegistry(ComponentRegistry registry);

    /**
     * Loads a packaged SceneCodec JSON path: read → decode → preload distinct Sprite texture keys →
     * exit previous → set current → enter new.
     *
     * @param path internal packaged path (forward slashes)
     * @throws EngineException if the file is missing, undecodable, or Sprite preload fails (message
     *     includes {@code path}); previous current is unchanged
     */
    void load(String path);

    /**
     * Replaces current with an already-built {@link Scene}: exit previous → set current → enter new.
     * Skips Files/Codec; does not sync-preload texture keys (path {@link #load} owns preload).
     */
    void switchTo(Scene scene);

    /**
     * Exits the current scene (if any) and clears {@link #current()} to null.
     */
    void unload();

    /** Active scene, or null after {@link #unload()} / before the first successful load/switch. */
    Scene current();

    /**
     * Registers a teardown hook invoked when a scene exits (replace or unload). The list starts
     * empty; ticket 14 will own bake via this seam.
     */
    void addTeardownHook(SceneTeardownHook hook);
}
