package io.github.worldsmiths.core;

/** Discrete load observation state for a {@link LoadHandle}. */
public enum LoadState {
    QUEUED,
    LOADING,
    COMPLETE,
    FAILED
}
