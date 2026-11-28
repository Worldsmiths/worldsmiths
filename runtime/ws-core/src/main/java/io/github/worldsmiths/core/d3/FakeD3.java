package io.github.worldsmiths.core.d3;

import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.FakeAssets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Recording {@link D3} for headless tests. Captures camera, lights, primitive builds, and batch
 * commands without touching GL. Seed pick-ray returns via {@link #seedPickRay} before {@link
 * Camera#getPickRay}; an empty seed queue throws {@link EngineException}.
 */
public final class FakeD3 implements D3 {
    private final FakeAssets assets;
    private final List<Command> commands = new ArrayList<Command>();
    private final Queue<Ray> seededPickRays = new ArrayDeque<Ray>();
    private boolean batchDisposed;
    private boolean modelsDisposed;
    private int nextModelId;
    private int nextInstanceId;
    private final Map<Integer, float[]> instanceDiffuse = new HashMap<Integer, float[]>();
    private final Map<Integer, float[]> instanceEmissive = new HashMap<Integer, float[]>();

    public FakeD3() {
        this(null);
    }

    /** When wired to {@link FakeAssets}, bind consults the mesh registry after MESH COMPLETE. */
    public FakeD3(FakeAssets assets) {
        this.assets = assets;
    }

    @Override
    public PerspectiveCamera createPerspectiveCamera(
            float fieldOfViewY, float viewportWidth, float viewportHeight) {
        commands.add(Command.createCamera(fieldOfViewY, viewportWidth, viewportHeight));
        return new FakePerspectiveCamera();
    }

    @Override
    public OrthographicCamera createOrthographicCamera(float viewportWidth, float viewportHeight) {
        commands.add(Command.createOrthoCamera(viewportWidth, viewportHeight));
        return new FakeOrthographicCamera();
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
        if (!(model instanceof FakeModel)) {
            throw new IllegalArgumentException("model must come from this D3 factory");
        }
        int modelId = ((FakeModel) model).id;
        int instanceId = nextInstanceId++;
        commands.add(Command.createInstance(modelId, instanceId));
        return new FakeModelInstance(instanceId);
    }

    @Override
    public Model createModelFromLoaded(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new EngineException("Cannot bind mesh: path is null or blank");
        }
        if (assets == null || !assets.isMeshLoaded(path)) {
            throw new EngineException("Cannot bind mesh before load COMPLETE: " + path);
        }
        int id = nextModelId++;
        commands.add(Command.createModelFromLoaded(path, id));
        return new FakeModel(id);
    }

    /** Queues the next {@link Camera#getPickRay} return value (FIFO). */
    public void seedPickRay(Ray ray) {
        if (ray == null) {
            throw new IllegalArgumentException("ray must be non-null");
        }
        seededPickRays.add(ray);
    }

    /** Convenience overload: queues a pick ray from origin and direction components. */
    public void seedPickRay(
            float originX,
            float originY,
            float originZ,
            float dirX,
            float dirY,
            float dirZ) {
        seedPickRay(new Ray(new Vec3(originX, originY, originZ), new Vec3(dirX, dirY, dirZ)));
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
        seededPickRays.clear();
        batchDisposed = false;
        modelsDisposed = false;
        nextModelId = 0;
        nextInstanceId = 0;
        instanceDiffuse.clear();
        instanceEmissive.clear();
    }

    /**
     * Headless seam: last diffuse set on an instance ({@code null} when unset). Sibling instances do
     * not share entries.
     */
    public float[] diffuseForTest(ModelInstance instance) {
        if (!(instance instanceof FakeModelInstance)) {
            throw new IllegalArgumentException("instance must come from this FakeD3");
        }
        return instanceDiffuse.get(((FakeModelInstance) instance).id);
    }

    private Ray nextPickRay(float screenX, float screenY) {
        commands.add(Command.pickRay(screenX, screenY));
        Ray seeded = seededPickRays.poll();
        if (seeded == null) {
            throw new EngineException("No seeded pick ray; call FakeD3.seedPickRay before getPickRay");
        }
        return new Ray(
                new Vec3(seeded.origin.x, seeded.origin.y, seeded.origin.z),
                new Vec3(seeded.direction.x, seeded.direction.y, seeded.direction.z));
    }

    private abstract class FakeCameraBase implements Camera {
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

        @Override
        public Ray getPickRay(float screenX, float screenY) {
            return nextPickRay(screenX, screenY);
        }
    }

    private final class FakePerspectiveCamera extends FakeCameraBase implements PerspectiveCamera {}

    private final class FakeOrthographicCamera extends FakeCameraBase implements OrthographicCamera {}

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
            int id = nextModelId++;
            commands.add(
                    Command.createSphereEmissive(
                            id,
                            width,
                            height,
                            depth,
                            divisionsU,
                            divisionsV,
                            r,
                            g,
                            b,
                            a,
                            er,
                            eg,
                            eb,
                            ea));
            return new FakeModel(id);
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
            int id = nextModelId++;
            commands.add(
                    Command.createBoxEmissive(
                            id, width, height, depth, r, g, b, a, er, eg, eb, ea));
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
            commands.add(
                    Command.instanceTrs(id, tx, ty, tz, eulerX, eulerY, eulerZ, sx, sy, sz));
        }

        @Override
        public void setDiffuse(float r, float g, float b, float a) {
            instanceDiffuse.put(id, new float[] {r, g, b, a});
            commands.add(Command.setDiffuse(id, r, g, b, a));
        }

        @Override
        public void setEmissive(float r, float g, float b, float a) {
            instanceEmissive.put(id, new float[] {r, g, b, a});
            commands.add(Command.setEmissive(id, r, g, b, a));
        }
    }

    private final class FakeModelBatch implements ModelBatch {
        @Override
        public void begin(Camera camera) {
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

    /** One recorded D3 call. */
    public static final class Command {
        public enum Kind {
            CREATE_CAMERA,
            CREATE_ORTHO_CAMERA,
            CAMERA_NEAR,
            CAMERA_FAR,
            CAMERA_POSITION,
            CAMERA_UP,
            CAMERA_LOOK_AT,
            CAMERA_UPDATE,
            CAMERA_PICK_RAY,
            CREATE_ENVIRONMENT,
            AMBIENT_LIGHT,
            DIRECTIONAL_LIGHT,
            CREATE_BUILDER,
            CREATE_SPHERE,
            CREATE_SPHERE_EMISSIVE,
            CREATE_BOX,
            CREATE_BOX_EMISSIVE,
            CREATE_MODEL_FROM_LOADED,
            CREATE_INSTANCE,
            INSTANCE_TRANSLATION,
            INSTANCE_TRANSFORM,
            INSTANCE_TRS,
            SET_DIFFUSE,
            SET_EMISSIVE,
            CREATE_BATCH,
            BATCH_BEGIN,
            BATCH_RENDER,
            BATCH_END,
            BATCH_DISPOSE,
            MODEL_DISPOSE
        }

        public final Kind kind;
        /** Geometry / vector: FOV, scalar, xyz.x, ortho width, or pick screenX (by kind). */
        public final float x;
        /** Geometry / vector: viewport width, xyz.y, ortho height, or pick screenY (by kind). */
        public final float y;
        /** Geometry / vector: viewport height, xyz.z, or primitive depth (by kind). */
        public final float z;
        /** Transform scale X when kind is {@link Kind#INSTANCE_TRANSFORM} or {@link Kind#INSTANCE_TRS}; else 0. */
        public final float scaleX;
        /** Transform scale Y when kind is {@link Kind#INSTANCE_TRANSFORM} or {@link Kind#INSTANCE_TRS}; else 0. */
        public final float scaleY;
        /** Transform scale Z when kind is {@link Kind#INSTANCE_TRANSFORM} or {@link Kind#INSTANCE_TRS}; else 0. */
        public final float scaleZ;
        public final float r;
        public final float g;
        public final float b;
        /** RGBA alpha when the kind carries color; else 0. */
        public final float alpha;
        /** Emissive R when kind is emissive create; else 0. */
        public final float emissiveR;
        /** Emissive G when kind is emissive create; else 0. */
        public final float emissiveG;
        /** Emissive B when kind is emissive create; else 0. */
        public final float emissiveB;
        /** Emissive A when kind is emissive create; else 0. */
        public final float emissiveA;
        /** Euler X degrees when kind is {@link Kind#INSTANCE_TRS}; else 0. */
        public final float eulerX;
        /** Euler Y degrees when kind is {@link Kind#INSTANCE_TRS}; else 0. */
        public final float eulerY;
        /** Euler Z degrees when kind is {@link Kind#INSTANCE_TRS}; else 0. */
        public final float eulerZ;
        public final int i0;
        public final int i1;
        /** Mesh path for {@link Kind#CREATE_MODEL_FROM_LOADED}; else {@code null}. */
        public final String path;

        private Command(
                Kind kind,
                float x,
                float y,
                float z,
                float scaleX,
                float scaleY,
                float scaleZ,
                float r,
                float g,
                float b,
                float alpha,
                float emissiveR,
                float emissiveG,
                float emissiveB,
                float emissiveA,
                float eulerX,
                float eulerY,
                float eulerZ,
                int i0,
                int i1,
                String path) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
            this.r = r;
            this.g = g;
            this.b = b;
            this.alpha = alpha;
            this.emissiveR = emissiveR;
            this.emissiveG = emissiveG;
            this.emissiveB = emissiveB;
            this.emissiveA = emissiveA;
            this.eulerX = eulerX;
            this.eulerY = eulerY;
            this.eulerZ = eulerZ;
            this.i0 = i0;
            this.i1 = i1;
            this.path = path;
        }

        static Command of(Kind kind) {
            return new Command(
                    kind, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0, 0, null);
        }

        static Command createCamera(float fov, float width, float height) {
            return new Command(
                    Kind.CREATE_CAMERA,
                    fov,
                    width,
                    height,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0,
                    0,
                    null);
        }

        static Command createOrthoCamera(float width, float height) {
            return new Command(
                    Kind.CREATE_ORTHO_CAMERA,
                    width,
                    height,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0,
                    0,
                    null);
        }

        static Command pickRay(float screenX, float screenY) {
            return new Command(
                    Kind.CAMERA_PICK_RAY,
                    screenX,
                    screenY,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0,
                    0,
                    null);
        }

        static Command scalar(Kind kind, float value) {
            return new Command(
                    kind, value, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0, 0, null);
        }

        static Command xyz(Kind kind, float x, float y, float z) {
            return new Command(
                    kind, x, y, z, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0, 0, null);
        }

        static Command rgba(Kind kind, float r, float g, float b, float a) {
            return new Command(
                    kind, 0f, 0f, 0f, 0f, 0f, 0f, r, g, b, a, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0, 0, null);
        }

        static Command directionalLight(
                float r, float g, float b, float dirX, float dirY, float dirZ) {
            return new Command(
                    Kind.DIRECTIONAL_LIGHT,
                    dirX,
                    dirY,
                    dirZ,
                    0f,
                    0f,
                    0f,
                    r,
                    g,
                    b,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0,
                    0,
                    null);
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
                    Kind.CREATE_SPHERE,
                    width,
                    height,
                    depth,
                    0f,
                    0f,
                    0f,
                    r,
                    g,
                    b,
                    a,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    id,
                    divisionsU + (divisionsV << 16),
                    null);
        }

        static Command createBox(
                int id, float width, float height, float depth, float r, float g, float b, float a) {
            return new Command(
                    Kind.CREATE_BOX,
                    width,
                    height,
                    depth,
                    0f,
                    0f,
                    0f,
                    r,
                    g,
                    b,
                    a,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    id,
                    0,
                    null);
        }

        static Command createSphereEmissive(
                int id,
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
            return new Command(
                    Kind.CREATE_SPHERE_EMISSIVE,
                    width,
                    height,
                    depth,
                    0f,
                    0f,
                    0f,
                    r,
                    g,
                    b,
                    a,
                    er,
                    eg,
                    eb,
                    ea,
                    0f,
                    0f,
                    0f,
                    id,
                    divisionsU + (divisionsV << 16),
                    null);
        }

        static Command createBoxEmissive(
                int id,
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
            return new Command(
                    Kind.CREATE_BOX_EMISSIVE,
                    width,
                    height,
                    depth,
                    0f,
                    0f,
                    0f,
                    r,
                    g,
                    b,
                    a,
                    er,
                    eg,
                    eb,
                    ea,
                    0f,
                    0f,
                    0f,
                    id,
                    0,
                    null);
        }

        static Command createModelFromLoaded(String path, int modelId) {
            return new Command(
                    Kind.CREATE_MODEL_FROM_LOADED,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    modelId,
                    0,
                    path);
        }

        static Command setDiffuse(int instanceId, float r, float g, float b, float a) {
            return rgba(Kind.SET_DIFFUSE, r, g, b, a).withInstanceId(instanceId);
        }

        static Command setEmissive(int instanceId, float r, float g, float b, float a) {
            return new Command(
                    Kind.SET_EMISSIVE,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    r,
                    g,
                    b,
                    a,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    instanceId,
                    0,
                    null);
        }

        private Command withInstanceId(int instanceId) {
            return new Command(
                    kind,
                    x,
                    y,
                    z,
                    scaleX,
                    scaleY,
                    scaleZ,
                    r,
                    g,
                    b,
                    alpha,
                    emissiveR,
                    emissiveG,
                    emissiveB,
                    emissiveA,
                    eulerX,
                    eulerY,
                    eulerZ,
                    instanceId,
                    i1,
                    path);
        }

        static Command createInstance(int modelId, int instanceId) {
            return new Command(
                    Kind.CREATE_INSTANCE,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    modelId,
                    instanceId,
                    null);
        }

        static Command instanceTranslation(int instanceId, float x, float y, float z) {
            return new Command(
                    Kind.INSTANCE_TRANSLATION,
                    x,
                    y,
                    z,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    instanceId,
                    0,
                    null);
        }

        static Command instanceTransform(
                int instanceId, float x, float y, float z, float sx, float sy, float sz) {
            return new Command(
                    Kind.INSTANCE_TRANSFORM,
                    x,
                    y,
                    z,
                    sx,
                    sy,
                    sz,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    instanceId,
                    0,
                    null);
        }

        static Command instanceTrs(
                int instanceId,
                float x,
                float y,
                float z,
                float eulerX,
                float eulerY,
                float eulerZ,
                float sx,
                float sy,
                float sz) {
            return new Command(
                    Kind.INSTANCE_TRS,
                    x,
                    y,
                    z,
                    sx,
                    sy,
                    sz,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    eulerX,
                    eulerY,
                    eulerZ,
                    instanceId,
                    0,
                    null);
        }

        static Command batchRender(int instanceId) {
            return new Command(
                    Kind.BATCH_RENDER,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    instanceId,
                    0,
                    null);
        }

        static Command modelDispose(int modelId) {
            return new Command(
                    Kind.MODEL_DISPOSE,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    modelId,
                    0,
                    null);
        }
    }
}
