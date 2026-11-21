package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import io.github.worldsmiths.core.thin3d.Model;
import io.github.worldsmiths.core.thin3d.ModelBuilder;

/** GDX-backed {@link ModelBuilder}. Not part of the author-facing API. */
final class GdxModelBuilder implements ModelBuilder {
    private static final int ATTRS =
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    private final com.badlogic.gdx.graphics.g3d.utils.ModelBuilder builder;

    GdxModelBuilder(com.badlogic.gdx.graphics.g3d.utils.ModelBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Model createSphere(
            float width,
            float height,
            float depth,
            int divisionsU,
            int divisionsV,
            float r,
            float g,
            float b,
            float a) {
        return new GdxModel(
                builder.createSphere(
                        width,
                        height,
                        depth,
                        divisionsU,
                        divisionsV,
                        diffuseMaterial(r, g, b, a),
                        ATTRS));
    }

    @Override
    public Model createBox(
            float width, float height, float depth, float r, float g, float b, float a) {
        return new GdxModel(
                builder.createBox(width, height, depth, diffuseMaterial(r, g, b, a), ATTRS));
    }

    @Override
    public Model createSphereEmissive(
            float width,
            float height,
            float depth,
            int divisionsU,
            int divisionsV,
            float r,
            float g,
            float b,
            float a,
            float er,
            float eg,
            float eb,
            float ea) {
        return new GdxModel(
                builder.createSphere(
                        width,
                        height,
                        depth,
                        divisionsU,
                        divisionsV,
                        diffuseEmissiveMaterial(r, g, b, a, er, eg, eb, ea),
                        ATTRS));
    }

    @Override
    public Model createBoxEmissive(
            float width,
            float height,
            float depth,
            float r,
            float g,
            float b,
            float a,
            float er,
            float eg,
            float eb,
            float ea) {
        return new GdxModel(
                builder.createBox(
                        width, height, depth, diffuseEmissiveMaterial(r, g, b, a, er, eg, eb, ea), ATTRS));
    }

    private static Material diffuseMaterial(float r, float g, float b, float a) {
        return new Material(ColorAttribute.createDiffuse(new Color(r, g, b, a)));
    }

    private static Material diffuseEmissiveMaterial(
            float r, float g, float b, float a, float er, float eg, float eb, float ea) {
        return new Material(
                ColorAttribute.createDiffuse(new Color(r, g, b, a)),
                ColorAttribute.createEmissive(new Color(er, eg, eb, ea)));
    }
}
