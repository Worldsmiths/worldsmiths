package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import io.github.worldsmiths.core.d3.Environment;

/** GDX-backed {@link Environment}. Not part of the author-facing API. */
final class GdxEnvironment implements Environment {
    private final com.badlogic.gdx.graphics.g3d.Environment environment;

    GdxEnvironment(com.badlogic.gdx.graphics.g3d.Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setAmbientLight(float r, float g, float b, float a) {
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, r, g, b, a));
    }

    @Override
    public void addDirectionalLight(
            float r, float g, float b, float dirX, float dirY, float dirZ) {
        environment.add(new DirectionalLight().set(r, g, b, dirX, dirY, dirZ));
    }

    com.badlogic.gdx.graphics.g3d.Environment delegate() {
        return environment;
    }
}
