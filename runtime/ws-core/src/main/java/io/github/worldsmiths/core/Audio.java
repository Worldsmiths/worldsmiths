package io.github.worldsmiths.core;

/**
 * One-shot sound factory from packaged <strong>internal</strong> paths. No LibGDX types in the
 * contract. Streaming Music is out of scope.
 *
 * <p>Missing or unloadable paths throw {@link EngineException} (unchecked). Playback is best-effort
 * — see {@link Sound}.
 */
public interface Audio {

    /**
     * Loads a one-shot sound from a packaged internal asset path.
     *
     * @param internalPath internal asset path (forward slashes, no leading slash required)
     * @return author-owned {@link Sound} handle
     * @throws EngineException if the path is missing or cannot be loaded
     */
    Sound createSound(String internalPath);
}
