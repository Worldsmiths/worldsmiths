package io.github.worldsmiths.core.thin3d;

/**
 * Immediate-mode 3D batch. Typical frame: {@link #begin} → {@link #render}… → {@link #end}.
 * Author-owned: call {@link #dispose()} once at shutdown (GDX-backed batches own GL resources).
 */
public interface ModelBatch {

    void begin(Camera camera);

    void render(ModelInstance instance, Environment environment);

    void end();

    /** Releases backend resources. Safe to call more than once. */
    void dispose();
}
