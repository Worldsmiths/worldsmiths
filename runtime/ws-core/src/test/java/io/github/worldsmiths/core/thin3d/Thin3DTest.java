package io.github.worldsmiths.core.thin3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.FakeEngineServices;
import java.util.List;
import org.junit.jupiter.api.Test;

class Thin3DTest {

    @Test
    void fakeOrthoCreatePoseBeginRecordsOrthoAndBatchBegin() {
        FakeThin3D thin3d = new FakeThin3D();

        OrthographicCamera camera = thin3d.createOrthographicCamera(12f, 12f);
        camera.setNear(1f);
        camera.setFar(100f);
        camera.setPosition(0f, 10f, 10f);
        camera.setUp(0f, 1f, 0f);
        camera.lookAt(0f, 0f, 0f);
        camera.update();

        ModelBatch batch = thin3d.createModelBatch();
        batch.begin(camera);
        batch.end();

        List<FakeThin3D.Command> commands = thin3d.commands();
        assertEquals(FakeThin3D.Command.Kind.CREATE_ORTHO_CAMERA, commands.get(0).kind);
        assertEquals(12f, commands.get(0).x, 1e-5f);
        assertEquals(12f, commands.get(0).y, 1e-5f);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_NEAR, commands.get(1).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_FAR, commands.get(2).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_POSITION, commands.get(3).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_UP, commands.get(4).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_LOOK_AT, commands.get(5).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_UPDATE, commands.get(6).kind);
        assertEquals(FakeThin3D.Command.Kind.CREATE_BATCH, commands.get(7).kind);
        assertEquals(FakeThin3D.Command.Kind.BATCH_BEGIN, commands.get(8).kind);
        assertEquals(FakeThin3D.Command.Kind.BATCH_END, commands.get(9).kind);
    }

    @Test
    void seededFakePickRayReturnsSeedAndRecordsScreenCoords() {
        FakeThin3D thin3d = new FakeThin3D();
        OrthographicCamera camera = thin3d.createOrthographicCamera(12f, 12f);
        Ray expected = new Ray(new Vec3(1f, 2f, 3f), new Vec3(0f, -1f, 0f));
        thin3d.seedPickRay(expected);

        Ray actual = camera.getPickRay(400f, 300f);

        assertEquals(expected, actual);
        assertEquals(1f, actual.origin.x, 1e-5f);
        assertEquals(2f, actual.origin.y, 1e-5f);
        assertEquals(3f, actual.origin.z, 1e-5f);
        assertEquals(0f, actual.direction.x, 1e-5f);
        assertEquals(-1f, actual.direction.y, 1e-5f);
        assertEquals(0f, actual.direction.z, 1e-5f);
        List<FakeThin3D.Command> commands = thin3d.commands();
        FakeThin3D.Command pick = commands.get(commands.size() - 1);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_PICK_RAY, pick.kind);
        assertEquals(400f, pick.x, 1e-5f);
        assertEquals(300f, pick.y, 1e-5f);
    }

    @Test
    void emptyFakePickSeedThrowsEngineException() {
        FakeThin3D thin3d = new FakeThin3D();
        OrthographicCamera camera = thin3d.createOrthographicCamera(12f, 12f);

        EngineException thrown =
                assertThrows(EngineException.class, () -> camera.getPickRay(10f, 20f));
        assertTrue(thrown.getMessage().contains("seed"));
    }

    @Test
    void fakeBagExposesStableThin3D() {
        FakeEngineServices services = new FakeEngineServices();

        assertNotNull(services.thin3d());
        assertSame(services.thin3d(), services.thin3d());
    }

    @Test
    void fakeThin3DRecordsPrimitiveDiffuseAlpha() {
        FakeThin3D thin3d = new FakeThin3D();
        ModelBuilder builder = thin3d.createModelBuilder();
        builder.createSphere(1f, 1f, 1f, 8, 8, 0.1f, 0.2f, 0.3f, 0.4f);
        builder.createBox(2f, 3f, 4f, 0.5f, 0.6f, 0.7f, 0.8f);

        List<FakeThin3D.Command> commands = thin3d.commands();
        assertEquals(FakeThin3D.Command.Kind.CREATE_SPHERE, commands.get(1).kind);
        assertEquals(1f, commands.get(1).x, 1e-5f);
        assertEquals(0.1f, commands.get(1).r, 1e-5f);
        assertEquals(0.2f, commands.get(1).g, 1e-5f);
        assertEquals(0.3f, commands.get(1).b, 1e-5f);
        assertEquals(0.4f, commands.get(1).alpha, 1e-5f);
        assertEquals(FakeThin3D.Command.Kind.CREATE_BOX, commands.get(2).kind);
        assertEquals(2f, commands.get(2).x, 1e-5f);
        assertEquals(3f, commands.get(2).y, 1e-5f);
        assertEquals(4f, commands.get(2).z, 1e-5f);
        assertEquals(0.5f, commands.get(2).r, 1e-5f);
        assertEquals(0.6f, commands.get(2).g, 1e-5f);
        assertEquals(0.7f, commands.get(2).b, 1e-5f);
        assertEquals(0.8f, commands.get(2).alpha, 1e-5f);
    }

    @Test
    void fakeThin3DRecordsCameraLightsPrimitivesBatchAndDispose() {
        FakeEngineServices services = new FakeEngineServices();
        FakeThin3D thin3d = services.thin3d();

        PerspectiveCamera camera = thin3d.createPerspectiveCamera(67f, 800f, 600f);
        camera.setNear(0.1f);
        camera.setFar(100f);
        camera.setPosition(1f, 2f, 3f);
        camera.setUp(0f, 1f, 0f);
        camera.lookAt(0f, 0f, 0f);
        camera.update();

        Environment environment = thin3d.createEnvironment();
        environment.setAmbientLight(0.45f, 0.45f, 0.45f, 1f);
        environment.addDirectionalLight(0.85f, 0.85f, 0.8f, -0.6f, -1f, -0.3f);

        ModelBuilder builder = thin3d.createModelBuilder();
        Model sphere = builder.createSphere(1f, 1f, 1f, 24, 24, 0.2f, 0.55f, 0.95f, 1f);
        Model box = builder.createBox(2f, 0.2f, 2f, 0.35f, 0.4f, 0.38f, 1f);
        ModelInstance sphereInstance = thin3d.createInstance(sphere);
        ModelInstance boxInstance = thin3d.createInstance(box);
        sphereInstance.setTranslationAndScaling(0.1f, 0.2f, 0.3f, 1f, 1f, 1f);
        boxInstance.setTranslation(0f, -0.1f, 0f);

        ModelBatch batch = thin3d.createModelBatch();
        batch.begin(camera);
        batch.render(boxInstance, environment);
        batch.render(sphereInstance, environment);
        batch.end();
        batch.dispose();
        sphere.dispose();
        box.dispose();

        List<FakeThin3D.Command> commands = thin3d.commands();
        assertEquals(FakeThin3D.Command.Kind.CREATE_CAMERA, commands.get(0).kind);
        assertEquals(67f, commands.get(0).x, 1e-5f);
        assertEquals(800f, commands.get(0).y, 1e-5f);
        assertEquals(600f, commands.get(0).z, 1e-5f);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_NEAR, commands.get(1).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_FAR, commands.get(2).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_POSITION, commands.get(3).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_UP, commands.get(4).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_LOOK_AT, commands.get(5).kind);
        assertEquals(FakeThin3D.Command.Kind.CAMERA_UPDATE, commands.get(6).kind);
        assertEquals(FakeThin3D.Command.Kind.CREATE_ENVIRONMENT, commands.get(7).kind);
        assertEquals(FakeThin3D.Command.Kind.AMBIENT_LIGHT, commands.get(8).kind);
        assertEquals(1f, commands.get(8).alpha, 1e-5f);
        assertEquals(FakeThin3D.Command.Kind.DIRECTIONAL_LIGHT, commands.get(9).kind);
        assertEquals(FakeThin3D.Command.Kind.CREATE_BUILDER, commands.get(10).kind);
        assertEquals(FakeThin3D.Command.Kind.CREATE_SPHERE, commands.get(11).kind);
        assertEquals(0.2f, commands.get(11).r, 1e-5f);
        assertEquals(1f, commands.get(11).alpha, 1e-5f);
        assertEquals(FakeThin3D.Command.Kind.CREATE_BOX, commands.get(12).kind);
        assertEquals(0.35f, commands.get(12).r, 1e-5f);
        assertEquals(1f, commands.get(12).alpha, 1e-5f);
        assertEquals(FakeThin3D.Command.Kind.CREATE_INSTANCE, commands.get(13).kind);
        assertEquals(FakeThin3D.Command.Kind.CREATE_INSTANCE, commands.get(14).kind);
        assertEquals(FakeThin3D.Command.Kind.INSTANCE_TRANSFORM, commands.get(15).kind);
        assertEquals(FakeThin3D.Command.Kind.INSTANCE_TRANSLATION, commands.get(16).kind);
        assertEquals(FakeThin3D.Command.Kind.CREATE_BATCH, commands.get(17).kind);
        assertEquals(FakeThin3D.Command.Kind.BATCH_BEGIN, commands.get(18).kind);
        assertEquals(FakeThin3D.Command.Kind.BATCH_RENDER, commands.get(19).kind);
        assertEquals(FakeThin3D.Command.Kind.BATCH_RENDER, commands.get(20).kind);
        assertEquals(FakeThin3D.Command.Kind.BATCH_END, commands.get(21).kind);
        assertEquals(FakeThin3D.Command.Kind.BATCH_DISPOSE, commands.get(22).kind);
        assertEquals(FakeThin3D.Command.Kind.MODEL_DISPOSE, commands.get(23).kind);
        assertEquals(FakeThin3D.Command.Kind.MODEL_DISPOSE, commands.get(24).kind);
        assertTrue(thin3d.batchDisposed());
        assertTrue(thin3d.modelsDisposed());
    }
}
