package io.github.worldsmiths.core.thin3d;

/**
 * Perspective camera from {@link Thin3D#createPerspectiveCamera}. No dispose. Typical frame: set
 * pose → {@link #lookAt} → {@link #update}, then pass to {@link ModelBatch#begin(Camera)}.
 */
public interface PerspectiveCamera extends Camera {}
