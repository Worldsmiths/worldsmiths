package io.github.worldsmiths.core;

/**
 * Unchecked engine failure. Used when a packaged internal file is missing or cannot be read via
 * {@link Files}, when a sound cannot be loaded via {@link Audio}, when a GDX-backed {@link
 * io.github.worldsmiths.core.thin3d.Camera#getPickRay} runs without a live graphics surface, or when
 * {@link io.github.worldsmiths.core.thin3d.FakeThin3D} {@code getPickRay} is called with an empty
 * seed queue; may grow to other fail-closed engine errors later.
 */
public class EngineException extends RuntimeException {

    public EngineException(String message) {
        super(message);
    }

    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
