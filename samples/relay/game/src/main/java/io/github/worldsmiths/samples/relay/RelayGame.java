package io.github.worldsmiths.samples.relay;

import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Font;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Sound;
import io.github.worldsmiths.core.Transform3D;
import io.github.worldsmiths.core.WorldsmithsGame;
import io.github.worldsmiths.core.thin3d.Environment;
import io.github.worldsmiths.core.thin3d.Model;
import io.github.worldsmiths.core.thin3d.ModelBatch;
import io.github.worldsmiths.core.thin3d.ModelBuilder;
import io.github.worldsmiths.core.thin3d.ModelInstance;
import io.github.worldsmiths.core.thin3d.PerspectiveCamera;
import io.github.worldsmiths.core.thin3d.Thin3D;
import io.github.worldsmiths.core.ui.Button;
import io.github.worldsmiths.core.ui.Insets;
import io.github.worldsmiths.core.ui.Theme;
import io.github.worldsmiths.core.ui.UiKit;

/**
 * P2 relay: ordered two-leg race (+X then −X), hard run-par FAIL, freeze-on-terminal. Keyboard
 * WASD/arrows + touch drag-stick; R or tap restarts when terminal. Plays a packaged one-shot cue on
 * rising CLEAR via {@code services.audio()}. Draws elapsed + live {@code N/M} (+ CLEAR/FAIL) via
 * {@code services.ui()} ({@link Font}); window title dual-writes thinly.
 */
public final class RelayGame implements WorldsmithsGame {

    /** Packaged internal path for the leg-complete one-shot cue. */
    public static final String LEG_COMPLETE_CUE = "audio/leg-complete.wav";

    /** Packaged internal BitmapFont path for the run-elapsed HUD. */
    public static final String HUD_FONT = "fonts/hud.fnt";

    /** UiKit panel ids (headless tests assert FakeUiKit recording). */
    static final String KIT_GO_ID = "relay-go";
    static final String KIT_LABEL_ID = "relay-label";
    private static final String KIT_GO_LABEL = "Go";
    private static final String KIT_STATUS_TEXT = "Relay kit";

    private static final float FLOOR_HALF = 5f;
    private static final float FLOOR_THICKNESS = 0.2f;
    private static final float GATE_MESH_SIZE = RelayController.CHECKPOINT_HALF * 2f;
    private static final float GATE_MESH_HEIGHT = 1.2f;
    private static final float HUD_X = 8f;
    private static final float HUD_Y = 24f;

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
    private Sound legCompleteCue;
    private Font hudFont;
    private float runElapsed;

    private boolean trackingTouch;
    private float touchOriginX;
    private float touchOriginY;
    private float touchLastX;
    private float touchLastY;

    private String lastTitle = "";
    private boolean wasClear;
    private boolean uiKitBuilt;

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
        legCompleteCue = services.audio().createSound(LEG_COMPLETE_CUE);
        hudFont = services.ui().createFont(HUD_FONT);
        runElapsed = 0f;
        wasClear = controller.cleared();
        publishTitle();
        buildUiKitPanel((int) w, (int) h);
    }

    @Override
    public void update(float delta) {
        if (!controller.terminal()) {
            runElapsed += delta;
        }
        applyInput(delta);
        syncScene();
        playClearCueIfNeeded();
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
        drawHud();
        drawUiKitPanel();
    }

    @Override
    public void dispose() {
        if (hudFont != null) {
            hudFont.dispose();
            hudFont = null;
        }
        if (legCompleteCue != null) {
            legCompleteCue.dispose();
            legCompleteCue = null;
        }
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
        if (services != null && uiKitBuilt) {
            services.uiKit().clear();
            uiKitBuilt = false;
        }
    }

    /** Package-visible test seam for post-advance gate sync. */
    Transform3D activeGateTransform() {
        return activeGateTransform;
    }

    /** Writes controller state into scene transforms. Package-visible test seam. */
    void syncScene() {
        if (scene == null || agentTransform == null) {
            return;
        }
        agentTransform.x = controller.agentX();
        agentTransform.y = controller.agentY();
        agentTransform.z = controller.agentZ();
        if (activeGateTransform != null) {
            float[] active = controller.activeCheckpointCenter();
            activeGateTransform.x = active[0];
            activeGateTransform.y = GATE_MESH_HEIGHT * 0.5f;
            activeGateTransform.z = active[1];
        }
        if (inactiveGateTransform != null) {
            float[] inactive = controller.inactiveCheckpointCenter();
            inactiveGateTransform.x = inactive[0];
            inactiveGateTransform.y = GATE_MESH_HEIGHT * 0.5f;
            inactiveGateTransform.z = inactive[1];
        }
    }

    /** Window title format. Package-visible test seam. */
    static String formatTitle(RelayController.CourseState state) {
        if (state == RelayController.CourseState.CLEAR) {
            return "Relay — CLEAR";
        }
        if (state == RelayController.CourseState.FAIL) {
            return "Relay — FAIL";
        }
        return "Relay — running";
    }

    /** Run-elapsed HUD text (seconds + live N/M + CLEAR/FAIL). Package-visible test seam. */
    static String formatElapsedHud(float runElapsedSeconds, RelayController controller) {
        String base =
                formatOneDecimal(runElapsedSeconds)
                        + "  "
                        + controller.currentLeg()
                        + "/"
                        + controller.totalLegs();
        if (controller.courseState() == RelayController.CourseState.CLEAR) {
            return base + " CLEAR";
        }
        if (controller.courseState() == RelayController.CourseState.FAIL) {
            return base + " FAIL";
        }
        return base;
    }

    private static String formatOneDecimal(float value) {
        int tenths = Math.round(value * 10f);
        int whole = tenths / 10;
        int frac = Math.abs(tenths % 10);
        return whole + "." + frac;
    }

    private void applyInput(float delta) {
        if (services == null) {
            controller.update(delta, 0f, 0f, runElapsed);
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
            runElapsed = 0f;
            wasClear = false;
            syncScene();
            publishTitle();
            return;
        }
        controller.update(delta, intent.axisX(), intent.axisZ(), runElapsed);
    }

    private void drawHud() {
        if (hudFont == null) {
            return;
        }
        hudFont.drawString(formatElapsedHud(runElapsed, controller), HUD_X, HUD_Y);
    }

    private void buildUiKitPanel(int width, int height) {
        if (services == null) {
            return;
        }
        UiKit kit = services.uiKit();
        kit.setTheme(Theme.defaults());
        kit.setViewport(width, height, Insets.ZERO, 1f);
        kit.clear();
        Button go = kit.button(KIT_GO_ID, KIT_GO_LABEL);
        kit.label(KIT_LABEL_ID, KIT_STATUS_TEXT);
        kit.focusScope().add(go);
        kit.layout();
        uiKitBuilt = true;
    }

    private void drawUiKitPanel() {
        if (services == null || !uiKitBuilt || hudFont == null) {
            return;
        }
        float w = services.graphics().getWidth();
        float h = services.graphics().getHeight();
        if (w <= 0f) {
            w = 800f;
        }
        if (h <= 0f) {
            h = 600f;
        }
        UiKit kit = services.uiKit();
        kit.setViewport((int) w, (int) h, Insets.ZERO, 1f);
        kit.layout();
        kit.draw(services.draw2d(), hudFont);
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

    private void playClearCueIfNeeded() {
        boolean clear = controller.cleared();
        if (clear && !wasClear && legCompleteCue != null) {
            legCompleteCue.play();
        }
        wasClear = clear;
    }

    private void publishTitleIfChanged() {
        String title = formatTitle(controller.courseState());
        if (!title.equals(lastTitle)) {
            publishTitle();
        }
    }

    private void publishTitle() {
        lastTitle = formatTitle(controller.courseState());
        if (services != null) {
            services.graphics().setTitle(lastTitle);
        }
    }
}
