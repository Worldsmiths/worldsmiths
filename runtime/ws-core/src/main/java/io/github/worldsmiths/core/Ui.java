package io.github.worldsmiths.core;

/**
 * Bitmap font factory from packaged <strong>internal</strong> paths. No LibGDX types in the
 * contract. Widgets are out of scope.
 *
 * <p>Missing or unloadable paths throw {@link EngineException} (unchecked). Drawing is best-effort
 * — see {@link Font}.
 */
public interface Ui {

    /**
     * Loads a bitmap font from a packaged internal asset path.
     *
     * @param internalPath internal asset path (forward slashes, no leading slash required)
     * @return author-owned {@link Font} handle
     * @throws EngineException if the path is missing or cannot be loaded
     */
    Font createFont(String internalPath);
}
