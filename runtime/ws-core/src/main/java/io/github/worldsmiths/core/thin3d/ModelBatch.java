package io.github.worldsmiths.core.thin3d;

/**
 * Immediate-mode 3D batch. Typical frame: {@link #begin} → {@link #render}… → {@link #end}. Call
 * {@link #dispose()} once at shutdown when this instance owns GL resources.
 */
public interface ModelBatch {

    void begin(PerspectiveCamera camera);

    void render(ModelInstance instance, Environment environment);

    void end();

    /** Releases backend resources. Safe to call more than once. */
    void dispose();
}
