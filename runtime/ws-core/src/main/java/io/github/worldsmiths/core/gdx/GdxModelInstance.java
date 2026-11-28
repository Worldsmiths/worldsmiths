package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import io.github.worldsmiths.core.d3.ModelInstance;

/** GDX-backed {@link ModelInstance}. Not part of the author-facing API. */
final class GdxModelInstance implements ModelInstance {
    final com.badlogic.gdx.graphics.g3d.ModelInstance delegate;

    GdxModelInstance(com.badlogic.gdx.graphics.g3d.ModelInstance delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setTranslation(float x, float y, float z) {
        delegate.transform.setToTranslation(x, y, z);
    }

    @Override
    public void setTranslationAndScaling(
            float x, float y, float z, float scaleX, float scaleY, float scaleZ) {
        delegate.transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
    }

    @Override
    public void setTranslationRotationScaling(
            float tx,
            float ty,
            float tz,
            float eulerX,
            float eulerY,
            float eulerZ,
            float sx,
            float sy,
            float sz) {
        delegate.transform.idt();
        delegate.transform.translate(tx, ty, tz);
        delegate.transform.rotate(Vector3.X, eulerX);
        delegate.transform.rotate(Vector3.Y, eulerY);
        delegate.transform.rotate(Vector3.Z, eulerZ);
        delegate.transform.scale(sx, sy, sz);
    }

    @Override
    public void setDiffuse(float r, float g, float b, float a) {
        applyColorAttribute(ColorAttribute.Diffuse, r, g, b, a);
    }

    @Override
    public void setEmissive(float r, float g, float b, float a) {
        applyColorAttribute(ColorAttribute.Emissive, r, g, b, a);
    }

    private void applyColorAttribute(long attribute, float r, float g, float b, float a) {
        Color color = new Color(r, g, b, a);
        for (Material material : delegate.materials) {
            if (material == null) {
                continue;
            }
            if (attribute == ColorAttribute.Diffuse) {
                material.set(ColorAttribute.createDiffuse(color));
            } else if (attribute == ColorAttribute.Emissive) {
                material.set(ColorAttribute.createEmissive(color));
            }
        }
    }
}
