package io.github.worldsmiths.core.d3;

/**
 * Drawable placement of a {@link Model}. Transform is overwritten each call (not incremental). No
 * dispose.
 */
public interface ModelInstance {

    void setTranslation(float x, float y, float z);

    void setTranslationAndScaling(float x, float y, float z, float scaleX, float scaleY, float scaleZ);

    /**
     * Full overwrite of translation, euler rotation (degrees, X then Y then Z), and scale.
     */
    void setTranslationRotationScaling(
            float tx,
            float ty,
            float tz,
            float eulerX,
            float eulerY,
            float eulerZ,
            float sx,
            float sy,
            float sz);

    /** Instance-local diffuse tint applied to all mesh parts (RGBA, 0–1). */
    void setDiffuse(float r, float g, float b, float a);

    /** Instance-local emissive tint applied to all mesh parts (RGBA, 0–1). */
    void setEmissive(float r, float g, float b, float a);
}
