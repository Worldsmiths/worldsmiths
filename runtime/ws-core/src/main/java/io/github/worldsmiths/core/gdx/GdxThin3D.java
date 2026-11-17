package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import io.github.worldsmiths.core.thin3d.Environment;
import io.github.worldsmiths.core.thin3d.Model;
import io.github.worldsmiths.core.thin3d.ModelBatch;
import io.github.worldsmiths.core.thin3d.ModelBuilder;
import io.github.worldsmiths.core.thin3d.ModelInstance;
import io.github.worldsmiths.core.thin3d.PerspectiveCamera;
import io.github.worldsmiths.core.thin3d.Thin3D;

/**
 * GDX-backed {@link Thin3D}. Not part of the author-facing API — backends assemble this via
 * {@link GdxEngineServices}.
 */
final class GdxThin3D implements Thin3D {
    private static final int ATTRS =
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    GdxThin3D() {}

    @Override
    public PerspectiveCamera createPerspectiveCamera(
            float fieldOfViewY, float viewportWidth, float viewportHeight) {
        return new GdxPerspectiveCamera(
                new com.badlogic.gdx.graphics.PerspectiveCamera(
                        fieldOfViewY, viewportWidth, viewportHeight));
    }

    @Override
    public Environment createEnvironment() {
        return new GdxEnvironment(new com.badlogic.gdx.graphics.g3d.Environment());
    }

    @Override
    public ModelBuilder createModelBuilder() {
        return new GdxModelBuilder(new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder());
    }

    @Override
    public ModelBatch createModelBatch() {
        return new GdxModelBatch(new com.badlogic.gdx.graphics.g3d.ModelBatch());
    }

    @Override
    public ModelInstance createInstance(Model model) {
        if (!(model instanceof GdxModel)) {
            throw new IllegalArgumentException("model must come from Thin3D.createModelBuilder()");
        }
        return new GdxModelInstance(
                new com.badlogic.gdx.graphics.g3d.ModelInstance(((GdxModel) model).delegate));
    }

    private static final class GdxPerspectiveCamera implements PerspectiveCamera {
        private final com.badlogic.gdx.graphics.PerspectiveCamera camera;

        GdxPerspectiveCamera(com.badlogic.gdx.graphics.PerspectiveCamera camera) {
            this.camera = camera;
        }

        @Override
        public void setNear(float near) {
            camera.near = near;
        }

        @Override
        public void setFar(float far) {
            camera.far = far;
        }

        @Override
        public void setPosition(float x, float y, float z) {
            camera.position.set(x, y, z);
        }

        @Override
        public void setUp(float x, float y, float z) {
            camera.up.set(x, y, z);
        }

        @Override
        public void lookAt(float x, float y, float z) {
            camera.lookAt(x, y, z);
        }

        @Override
        public void update() {
            camera.update();
        }

        com.badlogic.gdx.graphics.PerspectiveCamera delegate() {
            return camera;
        }
    }

    private static final class GdxEnvironment implements Environment {
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

    private static final class GdxModelBuilder implements ModelBuilder {
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

        private static Material diffuseMaterial(float r, float g, float b, float a) {
            return new Material(ColorAttribute.createDiffuse(new Color(r, g, b, a)));
        }
    }

    private static final class GdxModel implements Model {
        final com.badlogic.gdx.graphics.g3d.Model delegate;
        private boolean disposed;

        GdxModel(com.badlogic.gdx.graphics.g3d.Model delegate) {
            this.delegate = delegate;
        }

        @Override
        public void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            delegate.dispose();
        }
    }

    private static final class GdxModelInstance implements ModelInstance {
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
    }

    private static final class GdxModelBatch implements ModelBatch {
        private final com.badlogic.gdx.graphics.g3d.ModelBatch batch;
        private boolean disposed;

        GdxModelBatch(com.badlogic.gdx.graphics.g3d.ModelBatch batch) {
            this.batch = batch;
        }

        @Override
        public void begin(PerspectiveCamera camera) {
            if (!(camera instanceof GdxPerspectiveCamera)) {
                throw new IllegalArgumentException(
                        "camera must come from Thin3D.createPerspectiveCamera()");
            }
            batch.begin(((GdxPerspectiveCamera) camera).delegate());
        }

        @Override
        public void render(ModelInstance instance, Environment environment) {
            if (!(instance instanceof GdxModelInstance)) {
                throw new IllegalArgumentException("instance must come from Thin3D.createInstance()");
            }
            if (!(environment instanceof GdxEnvironment)) {
                throw new IllegalArgumentException(
                        "environment must come from Thin3D.createEnvironment()");
            }
            batch.render(
                    ((GdxModelInstance) instance).delegate,
                    ((GdxEnvironment) environment).delegate());
        }

        @Override
        public void end() {
            batch.end();
        }

        @Override
        public void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            batch.dispose();
        }
    }
}
