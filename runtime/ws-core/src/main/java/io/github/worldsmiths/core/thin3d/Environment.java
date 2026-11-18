package io.github.worldsmiths.core.thin3d;

/**
 * Lighting environment for {@link ModelBatch#render(ModelInstance, Environment)}. Ambient plus
 * directional lights cover the D9b rollball set. No dispose.
 */
public interface Environment {

    void setAmbientLight(float r, float g, float b, float a);

    /**
     * Adds a directional light. {@code dirX/Y/Z} is the light direction (toward the scene; typically
     * negative Y for top-down).
     */
    void addDirectionalLight(float r, float g, float b, float dirX, float dirY, float dirZ);
}
