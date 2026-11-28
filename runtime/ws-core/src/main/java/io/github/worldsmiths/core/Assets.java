package io.github.worldsmiths.core;

/**
 * Async packaged-asset load facade from {@link EngineServices#assets()}. No LibGDX types.
 *
 * <p>{@link #request(String, AssetKind)} never throws for a missing or unloadable path — observe
 * {@link LoadState#FAILED} on the returned {@link LoadHandle}. MUSIC remains stubbed; MESH loads
 * via GDX backends (cooperative web / JVM AssetManager) and Fake seeds in tests.
 * later tickets and fail with an unsupported-kind message.
 */
public interface Assets {

    /**
     * Enqueues (or shares) a load for a packaged internal path. Idempotent for the same {@code
     * (kind, path)}: shares an in-flight handle or returns a completed one if already cached.
     *
     * @param path internal asset path (forward slashes); null/blank fails deterministically
     * @param kind asset kind (TEXTURE and MESH supported on GDX/Fake when seeded; MUSIC stub-fail)
     * @return non-null handle; never throws for miss / unloadable / unsupported kind
     */
    LoadHandle request(String path, AssetKind kind);
}
