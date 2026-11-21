package io.github.worldsmiths.core.thin3d;

/**
 * Builds primitive meshes in code. Diffuse color is RGBA; position+normal attributes are always
 * included so directional lights shade correctly. No dispose — dispose the {@link Model}s you create.
 */
public interface ModelBuilder {

    Model createSphere(
            float width,
            float height,
            float depth,
            int divisionsU,
            int divisionsV,
            float r,
            float g,
            float b,
            float a);

    Model createBox(float width, float height, float depth, float r, float g, float b, float a);

    /**
     * Sphere with baked diffuse and emissive RGBA. Geometry and diffuse args match {@link
     * #createSphere}; emissive follows as {@code er, eg, eb, ea}.
     */
    Model createSphereEmissive(
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
            float ea);

    /**
     * Box with baked diffuse and emissive RGBA. Geometry and diffuse args match {@link #createBox};
     * emissive follows as {@code er, eg, eb, ea}.
     */
    Model createBoxEmissive(
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
            float ea);
}
