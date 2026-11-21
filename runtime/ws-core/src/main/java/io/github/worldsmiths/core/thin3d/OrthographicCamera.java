package io.github.worldsmiths.core.thin3d;

/**
 * Orthographic camera from {@link Thin3D#createOrthographicCamera}. Pose-only (no zoom / setToOrtho).
 * No dispose. Typical frame: set pose → {@link #lookAt} → {@link #update}, then pass to {@link
 * ModelBatch#begin(Camera)}.
 */
public interface OrthographicCamera extends Camera {}
