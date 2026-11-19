package io.github.worldsmiths.core;

/**
 * Unchecked engine failure. Used when a packaged internal file is missing or cannot be read via
 * {@link Files}, or when a sound cannot be loaded via {@link Audio}; may grow to other fail-closed
 * engine errors later.
 */
public class EngineException extends RuntimeException {

    public EngineException(String message) {
        super(message);
    }

    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
