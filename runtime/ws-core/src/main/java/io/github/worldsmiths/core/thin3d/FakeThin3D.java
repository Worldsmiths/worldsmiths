package io.github.worldsmiths.core.thin3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recording {@link Thin3D} for headless tests. Captures camera, lights, primitive builds, and batch
 * commands without touching GL.
 */
public final class FakeThin3D implements Thin3D {
    private final List<Command> commands = new ArrayList<Command>();
    private boolean batchDisposed;
    private boolean modelsDisposed;
    private int nextModelId;
    private int nextInstanceId;

    @Override
    public PerspectiveCamera createPerspectiveCamera(
            float fieldOfViewY, float viewportWidth, float viewportHeight) {
        commands.add(Command.createCamera(fieldOfViewY, viewportWidth, viewportHeight));
        return new FakeCamera();
    }

    @Override
    public Environment createEnvironment() {
        commands.add(Command.of(Command.Kind.CREATE_ENVIRONMENT));
        return new FakeEnvironment();
    }

    @Override
    public ModelBuilder createModelBuilder() {
        commands.add(Command.of(Command.Kind.CREATE_BUILDER));
        return new FakeModelBuilder();
    }

    @Override
    public ModelBatch createModelBatch() {
        commands.add(Command.of(Command.Kind.CREATE_BATCH));
        return new FakeModelBatch();
    }

    @Override
    public ModelInstance createInstance(Model model) {
        int modelId = model instanceof FakeModel ? ((FakeModel) model).id : -1;
        int instanceId = nextInstanceId++;
        commands.add(Command.createInstance(modelId, instanceId));
        return new FakeModelInstance(instanceId);
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public boolean batchDisposed() {
        return batchDisposed;
    }

    public boolean modelsDisposed() {
        return modelsDisposed;
    }

    public void clear() {
        commands.clear();
        batchDisposed = false;
        modelsDisposed = false;
        nextModelId = 0;
        nextInstanceId = 0;
    }

    private final class FakeCamera implements PerspectiveCamera {
        @Override
        public void setNear(float near) {
            commands.add(Command.scalar(Command.Kind.CAMERA_NEAR, near));
        }

        @Override
        public void setFar(float far) {
            commands.add(Command.scalar(Command.Kind.CAMERA_FAR, far));
        }

        @Override
        public void setPosition(float x, float y, float z) {
            commands.add(Command.xyz(Command.Kind.CAMERA_POSITION, x, y, z));
        }

        @Override
        public void setUp(float x, float y, float z) {
            commands.add(Command.xyz(Command.Kind.CAMERA_UP, x, y, z));
        }

        @Override
        public void lookAt(float x, float y, float z) {
            commands.add(Command.xyz(Command.Kind.CAMERA_LOOK_AT, x, y, z));
        }

        @Override
        public void update() {
            commands.add(Command.of(Command.Kind.CAMERA_UPDATE));
        }
    }

    private final class FakeEnvironment implements Environment {
        @Override
        public void setAmbientLight(float r, float g, float b, float a) {
            commands.add(Command.rgba(Command.Kind.AMBIENT_LIGHT, r, g, b, a));
        }

        @Override
        public void addDirectionalLight(
                float r, float g, float b, float dirX, float dirY, float dirZ) {
            commands.add(Command.directionalLight(r, g, b, dirX, dirY, dirZ));
        }
    }

    private final class FakeModelBuilder implements ModelBuilder {
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
            int id = nextModelId++;
            commands.add(
                    Command.createSphere(id, width, height, depth, divisionsU, divisionsV, r, g, b, a));
            return new FakeModel(id);
        }

        @Override
        public Model createBox(
                float width, float height, float depth, float r, float g, float b, float a) {
            int id = nextModelId++;
            commands.add(Command.createBox(id, width, height, depth, r, g, b, a));
            return new FakeModel(id);
        }
    }

    private final class FakeModel implements Model {
        final int id;
        private boolean disposed;

        FakeModel(int id) {
            this.id = id;
        }

        @Override
        public void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            modelsDisposed = true;
            commands.add(Command.modelDispose(id));
        }
    }

    private final class FakeModelInstance implements ModelInstance {
        final int id;

        FakeModelInstance(int id) {
            this.id = id;
        }

        @Override
        public void setTranslation(float x, float y, float z) {
            commands.add(Command.instanceTranslation(id, x, y, z));
        }

        @Override
        public void setTranslationAndScaling(
                float x, float y, float z, float scaleX, float scaleY, float scaleZ) {
            commands.add(Command.instanceTransform(id, x, y, z, scaleX, scaleY, scaleZ));
        }
    }

    private final class FakeModelBatch implements ModelBatch {
        @Override
        public void begin(PerspectiveCamera camera) {
            commands.add(Command.of(Command.Kind.BATCH_BEGIN));
        }

        @Override
        public void render(ModelInstance instance, Environment environment) {
            int instanceId =
                    instance instanceof FakeModelInstance ? ((FakeModelInstance) instance).id : -1;
            commands.add(Command.batchRender(instanceId));
        }

        @Override
        public void end() {
            commands.add(Command.of(Command.Kind.BATCH_END));
        }

        @Override
        public void dispose() {
            batchDisposed = true;
            commands.add(Command.of(Command.Kind.BATCH_DISPOSE));
        }
    }

    /** One recorded Thin3D call. */
    public static final class Command {
        public enum Kind {
            CREATE_CAMERA,
            CAMERA_NEAR,
            CAMERA_FAR,
            CAMERA_POSITION,
            CAMERA_UP,
            CAMERA_LOOK_AT,
            CAMERA_UPDATE,
            CREATE_ENVIRONMENT,
            AMBIENT_LIGHT,
            DIRECTIONAL_LIGHT,
            CREATE_BUILDER,
            CREATE_SPHERE,
            CREATE_BOX,
            CREATE_INSTANCE,
            INSTANCE_TRANSLATION,
            INSTANCE_TRANSFORM,
            CREATE_BATCH,
            BATCH_BEGIN,
            BATCH_RENDER,
            BATCH_END,
            BATCH_DISPOSE,
            MODEL_DISPOSE
        }

        public final Kind kind;
        public final float a;
        public final float b;
        public final float c;
        public final float d;
        public final float e;
        public final float f;
        public final int i0;
        public final int i1;

        private Command(
                Kind kind,
                float a,
                float b,
                float c,
                float d,
                float e,
                float f,
                int i0,
                int i1) {
            this.kind = kind;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
            this.i0 = i0;
            this.i1 = i1;
        }

        static Command of(Kind kind) {
            return new Command(kind, 0f, 0f, 0f, 0f, 0f, 0f, 0, 0);
        }

        static Command createCamera(float fov, float width, float height) {
            return new Command(Kind.CREATE_CAMERA, fov, width, height, 0f, 0f, 0f, 0, 0);
        }

        static Command scalar(Kind kind, float value) {
            return new Command(kind, value, 0f, 0f, 0f, 0f, 0f, 0, 0);
        }

        static Command xyz(Kind kind, float x, float y, float z) {
            return new Command(kind, x, y, z, 0f, 0f, 0f, 0, 0);
        }

        static Command rgba(Kind kind, float r, float g, float b, float a) {
            return new Command(kind, r, g, b, a, 0f, 0f, 0, 0);
        }

        static Command directionalLight(
                float r, float g, float b, float dirX, float dirY, float dirZ) {
            return new Command(Kind.DIRECTIONAL_LIGHT, r, g, b, dirX, dirY, dirZ, 0, 0);
        }

        static Command createSphere(
                int id,
                float width,
                float height,
                float depth,
                int divisionsU,
                int divisionsV,
                float r,
                float g,
                float b,
                float a) {
            return new Command(
                    Kind.CREATE_SPHERE, width, height, depth, r, g, b, id, divisionsU + (divisionsV << 16));
        }

        static Command createBox(
                int id, float width, float height, float depth, float r, float g, float b, float a) {
            return new Command(Kind.CREATE_BOX, width, height, depth, r, g, b, id, 0);
        }

        static Command createInstance(int modelId, int instanceId) {
            return new Command(Kind.CREATE_INSTANCE, 0f, 0f, 0f, 0f, 0f, 0f, modelId, instanceId);
        }

        static Command instanceTranslation(int instanceId, float x, float y, float z) {
            return new Command(Kind.INSTANCE_TRANSLATION, x, y, z, 0f, 0f, 0f, instanceId, 0);
        }

        static Command instanceTransform(
                int instanceId, float x, float y, float z, float sx, float sy, float sz) {
            return new Command(Kind.INSTANCE_TRANSFORM, x, y, z, sx, sy, sz, instanceId, 0);
        }

        static Command batchRender(int instanceId) {
            return new Command(Kind.BATCH_RENDER, 0f, 0f, 0f, 0f, 0f, 0f, instanceId, 0);
        }

        static Command modelDispose(int modelId) {
            return new Command(Kind.MODEL_DISPOSE, 0f, 0f, 0f, 0f, 0f, 0f, modelId, 0);
        }
    }
}
