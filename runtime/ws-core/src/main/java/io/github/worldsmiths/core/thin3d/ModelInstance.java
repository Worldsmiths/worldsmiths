package io.github.worldsmiths.core.thin3d;

/**
 * Drawable placement of a {@link Model}. Transform is overwritten each call (not incremental). No
 * dispose.
 */
public interface ModelInstance {

    void setTranslation(float x, float y, float z);

    void setTranslationAndScaling(float x, float y, float z, float scaleX, float scaleY, float scaleZ);
}
