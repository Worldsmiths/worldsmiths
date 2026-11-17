package io.github.worldsmiths.samples.rollball;

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
 * D8 roll-a-ball: sample-owned ModelBatch + follow camera via engine {@link Thin3D}; controller →
 * {@link Transform3D} → ModelInstance transform each frame. Keyboard WASD/arrows + touch drag-stick;
 * R/Space or tap to restart when terminal.
 */
public final class RollballGame implements WorldsmithsGame {

    private static final float PICKUP_Y = 0.4f;
    private static final float FLOOR_THICKNESS = 0.2f;

    private final RollballController controller;
    private EngineServices services;
    private Scene scene;
    private Transform3D ballTransform;
    private final Transform3D[] pickupTransforms = new Transform3D[RollballController.PICKUP_COUNT];

    private ModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera camera;
    private Model ballModel;
    private Model pickupModel;
    private Model floorModel;
    private ModelInstance ballInstance;
    private ModelInstance floorInstance;
    private final ModelInstance[] pickupInstances =
            new ModelInstance[RollballController.PICKUP_COUNT];

    private boolean trackingTouch;
    private float touchOriginX;
    private float touchOriginY;
    private float touchLastX;
    private float touchLastY;

    private String lastTitle = "";

    public RollballGame() {
        this(new RollballController());
    }

    /** Package-visible for headless scene-sync tests with an injected controller. */
    RollballGame(RollballController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        scene = new Scene("rollball");
        ballTransform =
                new Transform3D(controller.ballX(), controller.ballY(), controller.ballZ());
        scene.add(new Node("ball").add(ballTransform));

        Transform3D floorTransform = new Transform3D(0f, -FLOOR_THICKNESS * 0.5f, 0f);
        scene.add(new Node("floor").add(floorTransform));

        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            float[] pos = controller.pickupPosition(i);
            Transform3D tx = new Transform3D(pos[0], PICKUP_Y, pos[1]);
            tx.visible = !controller.pickupCollected(i);
            pickupTransforms[i] = tx;
            scene.add(new Node("pickup-" + i).add(tx));
        }
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
        float ballDiam = RollballController.BALL_RADIUS * 2f;
        ballModel =
                builder.createSphere(
                        ballDiam, ballDiam, ballDiam, 24, 24, 0.2f, 0.55f, 0.95f, 1f);
        float pickupDiam = RollballController.PICKUP_RADIUS * 2f;
        pickupModel =
                builder.createSphere(
                        pickupDiam, pickupDiam, pickupDiam, 16, 16, 0.95f, 0.75f, 0.15f, 1f);
        float floorSize = RollballController.FLOOR_HALF * 2f;
        floorModel =
                builder.createBox(
                        floorSize, FLOOR_THICKNESS, floorSize, 0.35f, 0.4f, 0.38f, 1f);

        ballInstance = thin3d.createInstance(ballModel);
        floorInstance = thin3d.createInstance(floorModel);
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            pickupInstances[i] = thin3d.createInstance(pickupModel);
        }

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
        modelBatch.render(ballInstance, environment);
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            if (pickupTransforms[i] != null
                    && pickupTransforms[i].visible
                    && pickupInstances[i] != null) {
                modelBatch.render(pickupInstances[i], environment);
            }
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        if (modelBatch != null) {
            modelBatch.dispose();
            modelBatch = null;
        }
        if (ballModel != null) {
            ballModel.dispose();
            ballModel = null;
        }
        if (pickupModel != null) {
            pickupModel.dispose();
            pickupModel = null;
        }
        if (floorModel != null) {
            floorModel.dispose();
            floorModel = null;
        }
    }

    /**
     * Writes controller state into scene {@link Transform3D} floats. Package-visible test seam.
     */
    void syncScene() {
        if (scene == null || ballTransform == null) {
            return;
        }
        ballTransform.x = controller.ballX();
        ballTransform.y = controller.ballY();
        ballTransform.z = controller.ballZ();
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            float[] pos = controller.pickupPosition(i);
            Transform3D tx = pickupTransforms[i];
            tx.x = pos[0];
            tx.y = PICKUP_Y;
            tx.z = pos[1];
            tx.visible = !controller.pickupCollected(i);
        }
    }

    /** Window title format. Package-visible test seam. */
    static String formatTitle(int score, boolean won, boolean failed) {
        String base = "Rollball — score: " + score;
        if (won) {
            return base + " — WIN";
        }
        if (failed) {
            return base + " — FAIL";
        }
        return base;
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
        RollballMoveIntent keyIntent =
                RollballMoveIntent.fromKeys(left, right, up, down, restartKey, terminal);
        RollballMoveIntent touchIntent = pollTouchIntent(terminal);
        RollballMoveIntent intent = RollballMoveIntent.compose(keyIntent, touchIntent);
        if (intent.restart()) {
            controller.restart();
            syncScene();
            publishTitle();
            return;
        }
        controller.update(delta, intent.axisX(), intent.axisZ());
    }

    /**
     * Polls engine {@link Input} for drag-stick / restart-tap. Finger-down sets stick origin; while
     * held, drag maps to axes; release zeros axes and may restart when terminal.
     */
    private RollballMoveIntent pollTouchIntent(boolean terminal) {
        Input input = services.input();
        if (input.justTouched()) {
            trackingTouch = true;
            touchOriginX = input.getX();
            touchOriginY = input.getY();
            touchLastX = touchOriginX;
            touchLastY = touchOriginY;
            return RollballTouchGestures.dragStick(
                    touchOriginX, touchOriginY, touchLastX, touchLastY, terminal);
        }
        if (trackingTouch && input.isTouched()) {
            touchLastX = input.getX();
            touchLastY = input.getY();
            return RollballTouchGestures.dragStick(
                    touchOriginX, touchOriginY, touchLastX, touchLastY, terminal);
        }
        if (trackingTouch) {
            trackingTouch = false;
            float upX = input.getX();
            float upY = input.getY();
            return RollballTouchGestures.release(touchOriginX, touchOriginY, upX, upY, terminal);
        }
        return RollballMoveIntent.of(0f, 0f, false);
    }

    private void applyInstanceTransforms() {
        if (ballInstance != null && ballTransform != null) {
            writeInstanceTransform(ballInstance, ballTransform);
        }
        if (floorInstance != null) {
            float y = -FLOOR_THICKNESS * 0.5f;
            floorInstance.setTranslation(0f, y, 0f);
        }
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            if (pickupInstances[i] != null && pickupTransforms[i] != null) {
                writeInstanceTransform(pickupInstances[i], pickupTransforms[i]);
            }
        }
    }

    private static void writeInstanceTransform(ModelInstance instance, Transform3D tx) {
        // Full overwrite each frame (not incremental translate).
        instance.setTranslationAndScaling(tx.x, tx.y, tx.z, tx.scale, tx.scale, tx.scale);
    }

    private void updateCamera() {
        if (camera == null) {
            return;
        }
        float[] eye = new float[3];
        float[] lookAt = new float[3];
        RollballFollowCamera.compute(
                controller.ballX(), controller.ballY(), controller.ballZ(), eye, lookAt);
        camera.setPosition(eye[0], eye[1], eye[2]);
        camera.setUp(0f, 1f, 0f);
        camera.lookAt(lookAt[0], lookAt[1], lookAt[2]);
        camera.update();
    }

    private void publishTitleIfChanged() {
        String title =
                formatTitle(controller.score(), controller.won(), controller.failed());
        if (!title.equals(lastTitle)) {
            publishTitle();
        }
    }

    private void publishTitle() {
        lastTitle = formatTitle(controller.score(), controller.won(), controller.failed());
        if (services != null) {
            services.graphics().setTitle(lastTitle);
        }
    }
}
