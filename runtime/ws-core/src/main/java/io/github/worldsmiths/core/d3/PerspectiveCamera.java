package io.github.worldsmiths.core.d3;

/**
 * Perspective camera from {@link D3#createPerspectiveCamera}. No dispose. Typical frame: set
 * pose → {@link #lookAt} → {@link #update}, then pass to {@link ModelBatch#begin(Camera)}.
 */
public interface PerspectiveCamera extends Camera {}
