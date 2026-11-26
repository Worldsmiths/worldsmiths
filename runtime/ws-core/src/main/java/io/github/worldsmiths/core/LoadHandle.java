package io.github.worldsmiths.core;

/**
 * Observation handle returned by {@link Assets#request(String, AssetKind)}. No LibGDX types.
 * Poll {@link #state()} / {@link #progress()} or register {@link #onTerminal(Runnable)}.
 */
public interface LoadHandle {

    /** Packaged internal path passed to {@link Assets#request}. */
    String path();

    AssetKind kind();

    LoadState state();

    /** Coarse progress in {@code [0, 1]}; discrete steps, not byte-level. */
    float progress();

    /**
     * Failure detail when {@link #state()} is {@link LoadState#FAILED}; otherwise {@code null} or
     * empty depending on impl.
     */
    String failureMessage();

    /**
     * Registers a listener that runs once when the handle reaches {@link LoadState#COMPLETE} or
     * {@link LoadState#FAILED}. Fires on the game-thread pump path (after pump, before update).
     */
    void onTerminal(Runnable listener);
}
