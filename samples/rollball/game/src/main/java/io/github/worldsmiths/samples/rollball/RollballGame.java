package io.github.worldsmiths.samples.rollball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Transform3D;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * D8 roll-a-ball: sample-owned ModelBatch + follow camera; controller → {@link Transform3D} →
 * ModelInstance.transform each frame. Keyboard WASD/arrows + touch drag-stick; R/Space or tap to
 * restart when terminal.
 */
public final class RollballGame implements WorldsmithsGame {

    private static final float PICKUP_Y = 0.4f;
    private static final float FLOOR_THICKNESS = 0.2f;
    private static final int ATTRS =
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    private final RollballController controller;
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
    public void create() {
        ModelBuilder builder = new ModelBuilder();
        float ballDiam = RollballController.BALL_RADIUS * 2f;
        ballModel =
                builder.createSphere(
                        ballDiam,
                        ballDiam,
                        ballDiam,
                        24,
                        24,
                        new Material(
                                ColorAttribute.createDiffuse(new Color(0.2f, 0.55f, 0.95f, 1f))),
                        ATTRS);
        float pickupDiam = RollballController.PICKUP_RADIUS * 2f;
        pickupModel =
                builder.createSphere(
                        pickupDiam,
                        pickupDiam,
                        pickupDiam,
                        16,
                        16,
                        new Material(
                                ColorAttribute.createDiffuse(new Color(0.95f, 0.75f, 0.15f, 1f))),
                        ATTRS);
        float floorSize = RollballController.FLOOR_HALF * 2f;
        floorModel =
                builder.createBox(
                        floorSize,
                        FLOOR_THICKNESS,
                        floorSize,
                        new Material(
                                ColorAttribute.createDiffuse(new Color(0.35f, 0.4f, 0.38f, 1f))),
                        ATTRS);

        ballInstance = new ModelInstance(ballModel);
        floorInstance = new ModelInstance(floorModel);
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            pickupInstances[i] = new ModelInstance(pickupModel);
        }

        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.45f, 1f));
        environment.add(new DirectionalLight().set(0.85f, 0.85f, 0.8f, -0.6f, -1f, -0.3f));

        float w = Gdx.graphics != null ? Gdx.graphics.getWidth() : 800f;
        float h = Gdx.graphics != null ? Gdx.graphics.getHeight() : 600f;
        camera = new PerspectiveCamera(67f, w, h);
        camera.near = 0.1f;
        camera.far = 100f;
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
        if (Gdx.input == null) {
            controller.update(delta, 0f, 0f);
            return;
        }
        boolean terminal = controller.terminal();
        boolean left =
                Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right =
                Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down =
                Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        boolean restartKey =
                Gdx.input.isKeyPressed(Input.Keys.R) || Gdx.input.isKeyPressed(Input.Keys.SPACE);
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
     * Polls {@code Gdx.input} for drag-stick / restart-tap. Finger-down sets stick origin; while
     * held, drag maps to axes; release zeros axes and may restart when terminal.
     */
    private RollballMoveIntent pollTouchIntent(boolean terminal) {
        if (Gdx.input.justTouched()) {
            trackingTouch = true;
            touchOriginX = Gdx.input.getX();
            touchOriginY = Gdx.input.getY();
            touchLastX = touchOriginX;
            touchLastY = touchOriginY;
            return RollballTouchGestures.dragStick(
                    touchOriginX, touchOriginY, touchLastX, touchLastY, terminal);
        }
        if (trackingTouch && Gdx.input.isTouched()) {
            touchLastX = Gdx.input.getX();
            touchLastY = Gdx.input.getY();
            return RollballTouchGestures.dragStick(
                    touchOriginX, touchOriginY, touchLastX, touchLastY, terminal);
        }
        if (trackingTouch) {
            trackingTouch = false;
            float upX = Gdx.input.getX();
            float upY = Gdx.input.getY();
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
            floorInstance.transform.setToTranslation(0f, y, 0f);
        }
        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            if (pickupInstances[i] != null && pickupTransforms[i] != null) {
                writeInstanceTransform(pickupInstances[i], pickupTransforms[i]);
            }
        }
    }

    private static void writeInstanceTransform(ModelInstance instance, Transform3D tx) {
        // Full overwrite each frame (not incremental translate).
        instance.transform.setToTranslationAndScaling(
                tx.x, tx.y, tx.z, tx.scale, tx.scale, tx.scale);
    }

    private void updateCamera() {
        if (camera == null) {
            return;
        }
        float[] eye = new float[3];
        float[] lookAt = new float[3];
        RollballFollowCamera.compute(
                controller.ballX(), controller.ballY(), controller.ballZ(), eye, lookAt);
        camera.position.set(eye[0], eye[1], eye[2]);
        camera.up.set(0f, 1f, 0f);
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
        if (Gdx.graphics != null) {
            Gdx.graphics.setTitle(lastTitle);
        }
    }
}
