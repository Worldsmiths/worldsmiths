package io.github.worldsmiths.core;

/**
 * Sync read of packaged <strong>internal</strong> text/bytes only. No LibGDX types in the contract.
 * External / local / absolute paths are out of D9 scope.
 *
 * <p>Missing internal paths throw {@link EngineException} (unchecked). On web backends, only
 * classpath/packaged internal assets are available — see the Files guide.
 */
public interface Files {

    /**
     * Reads a packaged internal file as UTF-8 text.
     *
     * @param path internal asset path (forward slashes, no leading slash required)
     * @throws EngineException if the path is missing or cannot be read
     */
    String readString(String path);

    /**
     * Reads a packaged internal file as raw bytes.
     *
     * @param path internal asset path (forward slashes, no leading slash required)
     * @throws EngineException if the path is missing or cannot be read
     */
    byte[] readBytes(String path);
}
