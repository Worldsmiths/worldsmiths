package io.github.worldsmiths.samples.relay;

import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Transform3D;
import io.github.worldsmiths.core.WorldsmithsGame;
import io.github.worldsmiths.core.thin3d.Environment;
import io.github.worldsmiths.core.thin3d.Model;
import io.github.worldsmiths.core.thin3d.ModelBatch;
import io.github.worldsmiths.core.thin3d.ModelBuilder;
import io.github.worldsmiths.core.thin3d.ModelInstance;
import io.github.worldsmiths.core.thin3d.PerspectiveCamera;
import io.github.worldsmiths.core.thin3d.Thin3D;

/**
 * P2 relay stub: agent on a ground plane, one active checkpoint (AABB), optional inactive second
 * gate mesh. Keyboard WASD/arrows + touch drag-stick; R or tap restarts after leg complete. Title
 * is the temporary HUD ({@code setTitle}).
 */
public final class RelayGame implements WorldsmithsGame {

    private static final float FLOOR_HALF = 5f;
    private static final float FLOOR_THICKNESS = 0.2f;
    private static final float GATE_MESH_SIZE = RelayController.CHECKPOINT_HALF * 2f;
    private static final float GATE_MESH_HEIGHT = 1.2f;

    private final RelayController controller;
    private EngineServices services;
    private Scene scene;
    private Transform3D agentTransform;
    private Transform3D activeGateTransform;
    private Transform3D inactiveGateTransform;

    private ModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera camera;
    private Model agentModel;
    private Model floorModel;
    private Model gateModel;
    private ModelInstance agentInstance;
    private ModelInstance floorInstance;
    private ModelInstance activeGateInstance;
    private ModelInstance inactiveGateInstance;

    private boolean trackingTouch;
    private float touchOriginX;
    private float touchOriginY;
    private float touchLastX;
    private float touchLastY;

    private String lastTitle = "";

    public RelayGame() {
        this(new RelayController());
    }

    /** Package-visible for headless tests with an injected controller. */
    RelayGame(RelayController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        scene = new Scene("relay");
        agentTransform =
                new Transform3D(controller.agentX(), controller.agentY(), controller.agentZ());
        scene.add(new Node("agent").add(agentTransform));

        Transform3D floorTransform = new Transform3D(0f, -FLOOR_THICKNESS * 0.5f, 0f);
        scene.add(new Node("floor").add(floorTransform));

        float[] active = controller.activeCheckpointCenter();
        activeGateTransform = new Transform3D(active[0], GATE_MESH_HEIGHT * 0.5f, active[1]);
        scene.add(new Node("gate-active").add(activeGateTransform));

        float[] inactive = controller.inactiveCheckpointCenter();
        inactiveGateTransform = new Transform3D(inactive[0], GATE_MESH_HEIGHT * 0.5f, inactive[1]);
        scene.add(new Node("gate-inactive").add(inactiveGateTransform));

        syncScene();
        return scene;
    }

    @Override
    public void create(EngineServices services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        this.services = services;
        Thin3D thin3d = services.thin3d();
        ModelBuilder builder = thin3d.createModelBuilder();

        float agentDiam = RelayController.AGENT_RADIUS * 2f;
        agentModel =
                builder.createSphere(
                        agentDiam, agentDiam, agentDiam, 24, 24, 0.25f, 0.7f, 0.95f, 1f);
        float floorSize = FLOOR_HALF * 2f;
        floorModel =
                builder.createBox(
                        floorSize, FLOOR_THICKNESS, floorSize, 0.32f, 0.38f, 0.34f, 1f);
        // Gate meshes are boxes matching the AABB checkpoint volume (implementer choice: AABB).
        gateModel =
                builder.createBox(
                        GATE_MESH_SIZE, GATE_MESH_HEIGHT, GATE_MESH_SIZE, 0.9f, 0.55f, 0.15f, 0.85f);

        agentInstance = thin3d.createInstance(agentModel);
        floorInstance = thin3d.createInstance(floorModel);
        activeGateInstance = thin3d.createInstance(gateModel);
        inactiveGateInstance = thin3d.createInstance(gateModel);

        modelBatch = thin3d.createModelBatch();
        environment = thin3d.createEnvironment();
        environment.setAmbientLight(0.45f, 0.45f, 0.45f, 1f);
        environment.addDirectionalLight(0.85f, 0.85f, 0.8f, -0.6f, -1f, -0.3f);

        float w = services.graphics().getWidth();
        float h = services.graphics().getHeight();
        if (w <= 0f) {
            w = 800f;
        }
        if (h <= 0f) {
            h = 600f;
        }
        camera = thin3d.createPerspectiveCamera(67f, w, h);
        camera.setNear(0.1f);
        camera.setFar(100f);
        updateCamera();
        publishTitle();
    }

    @Override
    public void update(float delta) {
        applyInput(delta);
        syncScene();
        publishTitleIfChanged();
    }

    @Override
    public void render() {
        if (modelBatch == null || camera == null) {
            return;
        }
        applyInstanceTransforms();
        updateCamera();
        modelBatch.begin(camera);
        modelBatch.render(floorInstance, environment);
        modelBatch.render(inactiveGateInstance, environment);
        modelBatch.render(activeGateInstance, environment);
        modelBatch.render(agentInstance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        if (modelBatch != null) {
            modelBatch.dispose();
            modelBatch = null;
        }
        if (agentModel != null) {
            agentModel.dispose();
            agentModel = null;
        }
        if (floorModel != null) {
            floorModel.dispose();
            floorModel = null;
        }
        if (gateModel != null) {
            gateModel.dispose();
            gateModel = null;
        }
    }

    /** Writes controller state into scene transforms. Package-visible test seam. */
    void syncScene() {
        if (scene == null || agentTransform == null) {
            return;
        }
        agentTransform.x = controller.agentX();
        agentTransform.y = controller.agentY();
        agentTransform.z = controller.agentZ();
    }

    /** Window title format. Package-visible test seam. */
    static String formatTitle(boolean legComplete) {
        if (legComplete) {
            return "Relay — leg complete";
        }
        return "Relay — running";
    }

    private void applyInput(float delta) {
        if (services == null) {
            controller.update(delta, 0f, 0f);
            return;
        }
        Input input = services.input();
        boolean terminal = controller.terminal();
        boolean left = input.isKeyDown(Key.LEFT) || input.isKeyDown(Key.A);
        boolean right = input.isKeyDown(Key.RIGHT) || input.isKeyDown(Key.D);
        boolean up = input.isKeyDown(Key.UP) || input.isKeyDown(Key.W);
        boolean down = input.isKeyDown(Key.DOWN) || input.isKeyDown(Key.S);
        boolean restartKey = input.isKeyDown(Key.R) || input.isKeyDown(Key.SPACE);
        RelayMoveIntent keyIntent =
                RelayMoveIntent.fromKeys(left, right, up, down, restartKey, terminal);
        RelayMoveIntent touchIntent = pollTouchIntent(terminal);
        RelayMoveIntent intent = RelayMoveIntent.compose(keyIntent, touchIntent);
        if (intent.restart()) {
            controller.restart();
            syncScene();
            publishTitle();
            return;
        }
        controller.update(delta, intent.axisX(), intent.axisZ());
    }

    private RelayMoveIntent pollTouchIntent(boolean terminal) {
        Input input = services.input();
        if (input.justTouched()) {
            trackingTouch = true;
            touchOriginX = input.getX();
            touchOriginY = input.getY();
            touchLastX = touchOriginX;
            touchLastY = touchOriginY;
            return RelayTouchGestures.dragStick(
                    touchOriginX, touchOriginY, touchLastX, touchLastY, terminal);
        }
        if (trackingTouch && input.isTouched()) {
            touchLastX = input.getX();
            touchLastY = input.getY();
            return RelayTouchGestures.dragStick(
                    touchOriginX, touchOriginY, touchLastX, touchLastY, terminal);
        }
        if (trackingTouch) {
            trackingTouch = false;
            float upX = input.getX();
            float upY = input.getY();
            return RelayTouchGestures.release(touchOriginX, touchOriginY, upX, upY, terminal);
        }
        return RelayMoveIntent.of(0f, 0f, false);
    }

    private void applyInstanceTransforms() {
        if (agentInstance != null && agentTransform != null) {
            writeInstanceTransform(agentInstance, agentTransform);
        }
        if (floorInstance != null) {
            floorInstance.setTranslation(0f, -FLOOR_THICKNESS * 0.5f, 0f);
        }
        if (activeGateInstance != null && activeGateTransform != null) {
            writeInstanceTransform(activeGateInstance, activeGateTransform);
        }
        if (inactiveGateInstance != null && inactiveGateTransform != null) {
            writeInstanceTransform(inactiveGateInstance, inactiveGateTransform);
        }
    }

    private static void writeInstanceTransform(ModelInstance instance, Transform3D tx) {
        instance.setTranslationAndScaling(tx.x, tx.y, tx.z, tx.scale, tx.scale, tx.scale);
    }

    private void updateCamera() {
        if (camera == null) {
            return;
        }
        // Fixed overview camera — stub does not need a follow-cam.
        camera.setPosition(0f, 9f, 10f);
        camera.setUp(0f, 1f, 0f);
        camera.lookAt(0f, 0f, 0f);
        camera.update();
    }

    private void publishTitleIfChanged() {
        String title = formatTitle(controller.legComplete());
        if (!title.equals(lastTitle)) {
            publishTitle();
        }
    }

    private void publishTitle() {
        lastTitle = formatTitle(controller.legComplete());
        if (services != null) {
            services.graphics().setTitle(lastTitle);
        }
    }
}
