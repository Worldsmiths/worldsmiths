package io.github.worldsmiths.core;

/**
 * Sync texture load/cache from packaged <strong>internal</strong> paths. No LibGDX types in the
 * contract. Async loading is available via {@link EngineServices#assets()} ({@link Assets}).
 *
 * <p>Missing or unloadable paths throw {@link EngineException} (unchecked). {@link #preload} and
 * {@link #resolve} are idempotent — repeated calls for the same key hit the engine-owned cache.
 */
public interface Textures {

    /**
     * Sync-warms the texture cache for a packaged internal asset path.
     *
     * @param internalPath internal asset path (forward slashes, no leading slash required)
     * @throws EngineException if the path is missing or cannot be loaded
     */
    void preload(String internalPath);

    /**
     * Sync-resolves a packaged internal asset path into the texture cache (loads on miss).
     *
     * @param internalPath internal asset path (forward slashes, no leading slash required)
     * @throws EngineException if the path is missing or cannot be loaded
     */
    void resolve(String internalPath);
}
