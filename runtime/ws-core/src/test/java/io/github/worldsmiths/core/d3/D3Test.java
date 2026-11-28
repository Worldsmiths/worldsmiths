package io.github.worldsmiths.core.d3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.LoadState;
import java.util.List;
import org.junit.jupiter.api.Test;

class D3Test {

    @Test
    void fakeOrthoCreatePoseBeginRecordsOrthoAndBatchBegin() {
        FakeD3 d3 = new FakeD3();

        OrthographicCamera camera = d3.createOrthographicCamera(12f, 12f);
        camera.setNear(1f);
        camera.setFar(100f);
        camera.setPosition(0f, 10f, 10f);
        camera.setUp(0f, 1f, 0f);
        camera.lookAt(0f, 0f, 0f);
        camera.update();

        Environment environment = d3.createEnvironment();
        Model box = d3.createModelBuilder().createBox(1f, 1f, 1f, 1f, 1f, 1f, 1f);
        ModelInstance instance = d3.createInstance(box);

        ModelBatch batch = d3.createModelBatch();
        batch.begin(camera);
        batch.render(instance, environment);
        batch.end();

        List<FakeD3.Command> commands = d3.commands();
        assertEquals(FakeD3.Command.Kind.CREATE_ORTHO_CAMERA, commands.get(0).kind);
        assertEquals(12f, commands.get(0).x, 1e-5f);
        assertEquals(12f, commands.get(0).y, 1e-5f);
        assertEquals(FakeD3.Command.Kind.CAMERA_NEAR, commands.get(1).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_FAR, commands.get(2).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_POSITION, commands.get(3).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_UP, commands.get(4).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_LOOK_AT, commands.get(5).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_UPDATE, commands.get(6).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_ENVIRONMENT, commands.get(7).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_BUILDER, commands.get(8).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_BOX, commands.get(9).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_INSTANCE, commands.get(10).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_BATCH, commands.get(11).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_BEGIN, commands.get(12).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_RENDER, commands.get(13).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_END, commands.get(14).kind);
    }

    @Test
    void seededFakePickRayReturnsSeedAndRecordsScreenCoords() {
        FakeD3 d3 = new FakeD3();
        OrthographicCamera camera = d3.createOrthographicCamera(12f, 12f);
        Ray expected = new Ray(new Vec3(1f, 2f, 3f), new Vec3(0f, -1f, 0f));
        d3.seedPickRay(expected);

        Ray actual = camera.getPickRay(400f, 300f);

        assertEquals(expected, actual);
        assertNotSame(expected, actual);
        assertNotSame(expected.origin, actual.origin);
        assertNotSame(expected.direction, actual.direction);
        assertEquals(1f, actual.origin.x, 1e-5f);
        assertEquals(2f, actual.origin.y, 1e-5f);
        assertEquals(3f, actual.origin.z, 1e-5f);
        assertEquals(0f, actual.direction.x, 1e-5f);
        assertEquals(-1f, actual.direction.y, 1e-5f);
        assertEquals(0f, actual.direction.z, 1e-5f);
        List<FakeD3.Command> commands = d3.commands();
        FakeD3.Command pick = commands.get(commands.size() - 1);
        assertEquals(FakeD3.Command.Kind.CAMERA_PICK_RAY, pick.kind);
        assertEquals(400f, pick.x, 1e-5f);
        assertEquals(300f, pick.y, 1e-5f);
    }

    @Test
    void emptyFakePickSeedThrowsEngineException() {
        FakeD3 d3 = new FakeD3();
        OrthographicCamera camera = d3.createOrthographicCamera(12f, 12f);

        EngineException thrown =
                assertThrows(EngineException.class, () -> camera.getPickRay(10f, 20f));
        assertTrue(thrown.getMessage().contains("seed"));
    }

    @Test
    void fakeBagExposesStableD3() {
        FakeEngineServices services = new FakeEngineServices();

        assertNotNull(services.d3());
        assertSame(services.d3(), services.d3());
    }

    @Test
    void fakeD3RecordsPrimitiveDiffuseAlpha() {
        FakeD3 d3 = new FakeD3();
        ModelBuilder builder = d3.createModelBuilder();
        builder.createSphere(1f, 1f, 1f, 8, 8, 0.1f, 0.2f, 0.3f, 0.4f);
        builder.createBox(2f, 3f, 4f, 0.5f, 0.6f, 0.7f, 0.8f);

        List<FakeD3.Command> commands = d3.commands();
        assertEquals(FakeD3.Command.Kind.CREATE_SPHERE, commands.get(1).kind);
        assertEquals(1f, commands.get(1).x, 1e-5f);
        assertEquals(0.1f, commands.get(1).r, 1e-5f);
        assertEquals(0.2f, commands.get(1).g, 1e-5f);
        assertEquals(0.3f, commands.get(1).b, 1e-5f);
        assertEquals(0.4f, commands.get(1).alpha, 1e-5f);
        assertEquals(FakeD3.Command.Kind.CREATE_BOX, commands.get(2).kind);
        assertEquals(2f, commands.get(2).x, 1e-5f);
        assertEquals(3f, commands.get(2).y, 1e-5f);
        assertEquals(4f, commands.get(2).z, 1e-5f);
        assertEquals(0.5f, commands.get(2).r, 1e-5f);
        assertEquals(0.6f, commands.get(2).g, 1e-5f);
        assertEquals(0.7f, commands.get(2).b, 1e-5f);
        assertEquals(0.8f, commands.get(2).alpha, 1e-5f);
    }

    @Test
    void fakeD3RecordsBoxEmissiveDiffuseAndEmissiveRgba() {
        FakeD3 d3 = new FakeD3();
        ModelBuilder builder = d3.createModelBuilder();
        builder.createBoxEmissive(
                0.1f, 0.1f, 2f, 1f, 1f, 1f, 1f, 1f, 0.4f, 0.1f, 1f);

        List<FakeD3.Command> commands = d3.commands();
        FakeD3.Command cmd = commands.get(1);
        assertEquals(FakeD3.Command.Kind.CREATE_BOX_EMISSIVE, cmd.kind);
        assertEquals(0.1f, cmd.x, 1e-5f);
        assertEquals(0.1f, cmd.y, 1e-5f);
        assertEquals(2f, cmd.z, 1e-5f);
        assertEquals(1f, cmd.r, 1e-5f);
        assertEquals(1f, cmd.g, 1e-5f);
        assertEquals(1f, cmd.b, 1e-5f);
        assertEquals(1f, cmd.alpha, 1e-5f);
        assertEquals(1f, cmd.emissiveR, 1e-5f);
        assertEquals(0.4f, cmd.emissiveG, 1e-5f);
        assertEquals(0.1f, cmd.emissiveB, 1e-5f);
        assertEquals(1f, cmd.emissiveA, 1e-5f);
    }

    @Test
    void fakeD3RecordsSphereEmissiveDiffuseAndEmissiveRgba() {
        FakeD3 d3 = new FakeD3();
        ModelBuilder builder = d3.createModelBuilder();
        builder.createSphereEmissive(
                1f, 1f, 1f, 16, 16, 0.2f, 0.3f, 0.4f, 0.5f, 0.9f, 0.8f, 0.1f, 1f);

        List<FakeD3.Command> commands = d3.commands();
        FakeD3.Command cmd = commands.get(1);
        assertEquals(FakeD3.Command.Kind.CREATE_SPHERE_EMISSIVE, cmd.kind);
        assertEquals(1f, cmd.x, 1e-5f);
        assertEquals(1f, cmd.y, 1e-5f);
        assertEquals(1f, cmd.z, 1e-5f);
        assertEquals(0.2f, cmd.r, 1e-5f);
        assertEquals(0.3f, cmd.g, 1e-5f);
        assertEquals(0.4f, cmd.b, 1e-5f);
        assertEquals(0.5f, cmd.alpha, 1e-5f);
        assertEquals(0.9f, cmd.emissiveR, 1e-5f);
        assertEquals(0.8f, cmd.emissiveG, 1e-5f);
        assertEquals(0.1f, cmd.emissiveB, 1e-5f);
        assertEquals(1f, cmd.emissiveA, 1e-5f);
        assertEquals(16 + (16 << 16), cmd.i1);
    }

    @Test
    void fakeD3RecordsTranslationRotationScaling() {
        FakeD3 d3 = new FakeD3();
        ModelBuilder builder = d3.createModelBuilder();
        Model box = builder.createBox(1f, 1f, 1f, 1f, 1f, 1f, 1f);
        ModelInstance instance = d3.createInstance(box);
        instance.setTranslationRotationScaling(0f, 0.05f, 0f, 10f, 45f, 20f, 1f, 2f, 3f);

        List<FakeD3.Command> commands = d3.commands();
        FakeD3.Command cmd = commands.get(commands.size() - 1);
        assertEquals(FakeD3.Command.Kind.INSTANCE_TRS, cmd.kind);
        assertEquals(0f, cmd.x, 1e-5f);
        assertEquals(0.05f, cmd.y, 1e-5f);
        assertEquals(0f, cmd.z, 1e-5f);
        assertEquals(10f, cmd.eulerX, 1e-5f);
        assertEquals(45f, cmd.eulerY, 1e-5f);
        assertEquals(20f, cmd.eulerZ, 1e-5f);
        assertEquals(1f, cmd.scaleX, 1e-5f);
        assertEquals(2f, cmd.scaleY, 1e-5f);
        assertEquals(3f, cmd.scaleZ, 1e-5f);
    }

    @Test
    void fakeD3RecordsCameraLightsPrimitivesBatchAndDispose() {
        FakeEngineServices services = new FakeEngineServices();
        FakeD3 d3 = services.d3();

        PerspectiveCamera camera = d3.createPerspectiveCamera(67f, 800f, 600f);
        camera.setNear(0.1f);
        camera.setFar(100f);
        camera.setPosition(1f, 2f, 3f);
        camera.setUp(0f, 1f, 0f);
        camera.lookAt(0f, 0f, 0f);
        camera.update();

        Environment environment = d3.createEnvironment();
        environment.setAmbientLight(0.45f, 0.45f, 0.45f, 1f);
        environment.addDirectionalLight(0.85f, 0.85f, 0.8f, -0.6f, -1f, -0.3f);

        ModelBuilder builder = d3.createModelBuilder();
        Model sphere = builder.createSphere(1f, 1f, 1f, 24, 24, 0.2f, 0.55f, 0.95f, 1f);
        Model box = builder.createBox(2f, 0.2f, 2f, 0.35f, 0.4f, 0.38f, 1f);
        ModelInstance sphereInstance = d3.createInstance(sphere);
        ModelInstance boxInstance = d3.createInstance(box);
        sphereInstance.setTranslationAndScaling(0.1f, 0.2f, 0.3f, 1f, 1f, 1f);
        boxInstance.setTranslation(0f, -0.1f, 0f);

        ModelBatch batch = d3.createModelBatch();
        batch.begin(camera);
        batch.render(boxInstance, environment);
        batch.render(sphereInstance, environment);
        batch.end();
        batch.dispose();
        sphere.dispose();
        box.dispose();

        List<FakeD3.Command> commands = d3.commands();
        assertEquals(FakeD3.Command.Kind.CREATE_CAMERA, commands.get(0).kind);
        assertEquals(67f, commands.get(0).x, 1e-5f);
        assertEquals(800f, commands.get(0).y, 1e-5f);
        assertEquals(600f, commands.get(0).z, 1e-5f);
        assertEquals(FakeD3.Command.Kind.CAMERA_NEAR, commands.get(1).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_FAR, commands.get(2).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_POSITION, commands.get(3).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_UP, commands.get(4).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_LOOK_AT, commands.get(5).kind);
        assertEquals(FakeD3.Command.Kind.CAMERA_UPDATE, commands.get(6).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_ENVIRONMENT, commands.get(7).kind);
        assertEquals(FakeD3.Command.Kind.AMBIENT_LIGHT, commands.get(8).kind);
        assertEquals(1f, commands.get(8).alpha, 1e-5f);
        assertEquals(FakeD3.Command.Kind.DIRECTIONAL_LIGHT, commands.get(9).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_BUILDER, commands.get(10).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_SPHERE, commands.get(11).kind);
        assertEquals(0.2f, commands.get(11).r, 1e-5f);
        assertEquals(1f, commands.get(11).alpha, 1e-5f);
        assertEquals(FakeD3.Command.Kind.CREATE_BOX, commands.get(12).kind);
        assertEquals(0.35f, commands.get(12).r, 1e-5f);
        assertEquals(1f, commands.get(12).alpha, 1e-5f);
        assertEquals(FakeD3.Command.Kind.CREATE_INSTANCE, commands.get(13).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_INSTANCE, commands.get(14).kind);
        assertEquals(FakeD3.Command.Kind.INSTANCE_TRANSFORM, commands.get(15).kind);
        assertEquals(FakeD3.Command.Kind.INSTANCE_TRANSLATION, commands.get(16).kind);
        assertEquals(FakeD3.Command.Kind.CREATE_BATCH, commands.get(17).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_BEGIN, commands.get(18).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_RENDER, commands.get(19).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_RENDER, commands.get(20).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_END, commands.get(21).kind);
        assertEquals(FakeD3.Command.Kind.BATCH_DISPOSE, commands.get(22).kind);
        assertEquals(FakeD3.Command.Kind.MODEL_DISPOSE, commands.get(23).kind);
        assertEquals(FakeD3.Command.Kind.MODEL_DISPOSE, commands.get(24).kind);
        assertTrue(d3.batchDisposed());
        assertTrue(d3.modelsDisposed());
    }

    @Test
    void createModelFromLoadedBeforeMeshCompleteThrowsEngineException() {
        FakeEngineServices services = new FakeEngineServices();
        services.assets().putMesh("meshes/hero.gltf");
        services.assets().request("meshes/hero.gltf", AssetKind.MESH);

        EngineException thrown =
                assertThrows(
                        EngineException.class,
                        () -> services.d3().createModelFromLoaded("meshes/hero.gltf"));
        assertTrue(thrown.getMessage().toLowerCase().contains("complete"));
    }

    @Test
    void createModelFromLoadedBlankOrUnknownPathThrowsEngineException() {
        FakeEngineServices services = new FakeEngineServices();

        assertThrows(EngineException.class, () -> services.d3().createModelFromLoaded(""));
        assertThrows(EngineException.class, () -> services.d3().createModelFromLoaded("meshes/ghost.gltf"));
    }

    @Test
    void createModelFromLoadedAfterCompleteReturnsNewModelEachBind() {
        FakeEngineServices services = new FakeEngineServices();
        services.assets().putMesh("meshes/hero.gltf");
        services.assets().request("meshes/hero.gltf", AssetKind.MESH);
        services.assets().pump();
        assertEquals(LoadState.COMPLETE, services.assets().request("meshes/hero.gltf", AssetKind.MESH).state());

        Model first = services.d3().createModelFromLoaded("meshes/hero.gltf");
        Model second = services.d3().createModelFromLoaded("meshes/hero.gltf");
        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);

        ModelInstance instance = services.d3().createInstance(first);
        ModelBatch batch = services.d3().createModelBatch();
        Environment environment = services.d3().createEnvironment();
        OrthographicCamera camera = services.d3().createOrthographicCamera(10f, 10f);
        instance.setDiffuse(1f, 0.2f, 0.2f, 1f);
        instance.setEmissive(0.1f, 0.2f, 0.3f, 1f);
        batch.begin(camera);
        batch.render(instance, environment);
        batch.end();

        List<FakeD3.Command> commands = services.d3().commands();
        boolean sawBind = false;
        int bindCount = 0;
        boolean sawDiffuse = false;
        boolean sawEmissive = false;
        for (FakeD3.Command cmd : commands) {
            if (cmd.kind == FakeD3.Command.Kind.CREATE_MODEL_FROM_LOADED) {
                sawBind = true;
                bindCount++;
                assertEquals("meshes/hero.gltf", cmd.path);
            }
            if (cmd.kind == FakeD3.Command.Kind.SET_DIFFUSE) {
                sawDiffuse = true;
                assertEquals(1f, cmd.r, 1e-5f);
                assertEquals(0.2f, cmd.g, 1e-5f);
            }
            if (cmd.kind == FakeD3.Command.Kind.SET_EMISSIVE) {
                sawEmissive = true;
                assertEquals(0.1f, cmd.r, 1e-5f);
            }
        }
        assertTrue(sawBind);
        assertEquals(2, bindCount);
        assertTrue(sawDiffuse);
        assertTrue(sawEmissive);
    }

    @Test
    void setDiffuseOnOneSiblingInstanceLeavesOtherUntintedForProceduralModel() {
        FakeD3 d3 = new FakeD3();
        Model model = d3.createModelBuilder().createBox(1f, 1f, 1f, 0.4f, 0.4f, 0.4f, 1f);
        ModelInstance tinted = d3.createInstance(model);
        ModelInstance sibling = d3.createInstance(model);

        tinted.setDiffuse(1f, 0.1f, 0.1f, 1f);

        float[] tintedDiffuse = d3.diffuseForTest(tinted);
        assertNotNull(tintedDiffuse);
        assertEquals(1f, tintedDiffuse[0], 1e-5f);
        assertNull(d3.diffuseForTest(sibling));
    }

    @Test
    void setDiffuseOnOneSiblingInstanceLeavesOtherUntintedForLoadedModel() {
        FakeEngineServices services = new FakeEngineServices();
        services.assets().putMesh("meshes/hero.gltf");
        services.assets().request("meshes/hero.gltf", AssetKind.MESH);
        services.assets().pump();

        Model model = services.d3().createModelFromLoaded("meshes/hero.gltf");
        ModelInstance tinted = services.d3().createInstance(model);
        ModelInstance sibling = services.d3().createInstance(model);

        tinted.setDiffuse(0.2f, 0.8f, 0.3f, 1f);

        float[] tintedDiffuse = services.d3().diffuseForTest(tinted);
        assertNotNull(tintedDiffuse);
        assertEquals(0.8f, tintedDiffuse[1], 1e-5f);
        assertNull(services.d3().diffuseForTest(sibling));
    }

    @Test
    void disposingLoadedModelWrapperDoesNotEvictMeshRegistryOrBlockRebind() {
        FakeEngineServices services = new FakeEngineServices();
        String path = "meshes/hero.gltf";
        services.assets().putMesh(path);
        services.assets().request(path, AssetKind.MESH);
        services.assets().pump();
        assertTrue(services.assets().isMeshLoaded(path));

        Model first = services.d3().createModelFromLoaded(path);
        first.dispose();

        assertTrue(services.assets().isMeshLoaded(path));
        Model second = services.d3().createModelFromLoaded(path);
        assertNotNull(second);
        assertNotSame(first, second);
    }
}
