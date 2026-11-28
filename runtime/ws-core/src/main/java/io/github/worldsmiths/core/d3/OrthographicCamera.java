package io.github.worldsmiths.core.d3;

/**
 * Orthographic camera from {@link D3#createOrthographicCamera}. Pose-only (no zoom / setToOrtho).
 * No dispose. Typical frame: set pose → {@link #lookAt} → {@link #update}, then pass to {@link
 * ModelBatch#begin(Camera)}.
 */
public interface OrthographicCamera extends Camera {}
