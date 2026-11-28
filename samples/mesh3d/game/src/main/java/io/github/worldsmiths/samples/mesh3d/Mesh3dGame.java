package io.github.worldsmiths.samples.mesh3d;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;
import io.github.worldsmiths.core.d3.D3;
import io.github.worldsmiths.core.d3.Environment;
import io.github.worldsmiths.core.d3.Model;
import io.github.worldsmiths.core.d3.ModelBatch;
import io.github.worldsmiths.core.d3.ModelInstance;
import io.github.worldsmiths.core.d3.PerspectiveCamera;

/**
 * glTF MESH sample: requests packaged {@value #DEMO_GLTF_PATH}, binds after {@link
 * LoadState#COMPLETE}, draws via {@link D3}, and toggles diffuse/emissive for a visible tint change.
 * Game sources stay free of LibGDX and gdx-gltf types.
 */
public final class Mesh3dGame implements WorldsmithsGame {

    static final String DEMO_GLTF_PATH = "meshes/demo.gltf";
    private static final String LOG_TAG = "mesh3d";
    private static final float TOGGLE_INTERVAL_SEC = 2f;

    private EngineServices services;
    private LoadHandle meshHandle;
    private boolean meshBound;
    private boolean warmTint = true;
    private float tintTimer;

    private PerspectiveCamera camera;
    private Environment environment;
    private ModelBatch modelBatch;
    private Model meshModel;
    private ModelInstance meshInstance;

    @Override
    public void create(EngineServices services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        this.services = services;
        services.scenes().switchTo(new Scene("mesh3d"));
        services.graphics().setTitle("Mesh3d");
        meshHandle = services.assets().request(DEMO_GLTF_PATH, AssetKind.MESH);
        services.log().info(LOG_TAG, "loading " + DEMO_GLTF_PATH);

        D3 d3 = services.d3();
        float w = services.graphics().getWidth();
        float h = services.graphics().getHeight();
        if (w <= 0f) {
            w = 800f;
        }
        if (h <= 0f) {
            h = 600f;
        }
        camera = d3.createPerspectiveCamera(55f, w, h);
        camera.setNear(0.1f);
        camera.setFar(100f);
        camera.setPosition(0f, 0.8f, 2.2f);
        camera.setUp(0f, 1f, 0f);
        camera.lookAt(0f, 0f, 0f);
        camera.update();

        environment = d3.createEnvironment();
        environment.setAmbientLight(0.55f, 0.55f, 0.6f, 1f);
        environment.addDirectionalLight(0.9f, 0.9f, 0.85f, -0.4f, -1f, -0.2f);

        modelBatch = d3.createModelBatch();
    }

    @Override
    public void update(float delta) {
        if (services == null || meshHandle == null) {
            return;
        }
        LoadState state = meshHandle.state();
        if (state == LoadState.FAILED) {
            String detail = meshHandle.failureMessage();
            if (detail == null || detail.isEmpty()) {
                detail = "FAILED: " + meshHandle.path();
            }
            services.log().warn(LOG_TAG, detail);
            return;
        }
        if (!meshBound && state == LoadState.COMPLETE) {
            bindMesh();
        }
        if (meshBound && meshInstance != null) {
            tintTimer += delta;
            if (tintTimer >= TOGGLE_INTERVAL_SEC) {
                tintTimer = 0f;
                warmTint = !warmTint;
                applyTint();
            }
        }
    }

    @Override
    public void render() {
        if (modelBatch == null || camera == null || meshInstance == null) {
            return;
        }
        modelBatch.begin(camera);
        modelBatch.render(meshInstance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        if (modelBatch != null) {
            modelBatch.dispose();
            modelBatch = null;
        }
        if (meshModel != null) {
            meshModel.dispose();
            meshModel = null;
        }
        meshInstance = null;
    }

    private void bindMesh() {
        D3 d3 = services.d3();
        meshModel = d3.createModelFromLoaded(DEMO_GLTF_PATH);
        meshInstance = d3.createInstance(meshModel);
        meshInstance.setTranslationAndScaling(0f, 0f, 0f, 1f, 1f, 1f);
        meshBound = true;
        warmTint = true;
        applyTint();
        services.log().info(LOG_TAG, "bound " + DEMO_GLTF_PATH);
    }

    private void applyTint() {
        if (meshInstance == null) {
            return;
        }
        if (warmTint) {
            meshInstance.setDiffuse(1f, 0.35f, 0.2f, 1f);
            meshInstance.setEmissive(0.15f, 0.05f, 0f, 1f);
        } else {
            meshInstance.setDiffuse(0.25f, 0.45f, 1f, 1f);
            meshInstance.setEmissive(0f, 0.1f, 0.25f, 1f);
        }
    }

    /** Package-visible for headless tests. */
    LoadHandle meshLoadHandle() {
        return meshHandle;
    }

    /** Package-visible for headless tests. */
    boolean isMeshBound() {
        return meshBound;
    }
}