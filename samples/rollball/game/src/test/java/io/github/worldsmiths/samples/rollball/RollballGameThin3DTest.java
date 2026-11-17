package io.github.worldsmiths.samples.rollball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.thin3d.FakeThin3D;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Headless create/render coverage via {@link FakeThin3D} — no GL / display required. Sync-only tests
 * in {@link RollballGameSyncTest} still skip {@code create()}.
 */
class RollballGameThin3DTest {

    @Test
    void createAndRenderRecordPrimitivesBatchAndTitleWithoutGl() {
        FakeEngineServices services = new FakeEngineServices();
        services.graphics().setSize(800, 600);
        RollballGame game = new RollballGame();
        game.createScene();
        game.create(services);

        assertEquals("Rollball — score: 0", services.graphics().lastTitle());

        FakeThin3D thin3d = services.thin3d();
        List<FakeThin3D.Command> created = thin3d.commands();
        assertTrue(containsKind(created, FakeThin3D.Command.Kind.CREATE_SPHERE));
        assertTrue(containsKind(created, FakeThin3D.Command.Kind.CREATE_BOX));
        assertTrue(containsKind(created, FakeThin3D.Command.Kind.CREATE_BATCH));
        assertTrue(containsKind(created, FakeThin3D.Command.Kind.AMBIENT_LIGHT));
        assertTrue(containsKind(created, FakeThin3D.Command.Kind.DIRECTIONAL_LIGHT));
        assertTrue(containsKind(created, FakeThin3D.Command.Kind.CREATE_CAMERA));

        thin3d.clear();
        game.render();

        List<FakeThin3D.Command> frame = thin3d.commands();
        assertTrue(containsKind(frame, FakeThin3D.Command.Kind.BATCH_BEGIN));
        assertTrue(containsKind(frame, FakeThin3D.Command.Kind.BATCH_RENDER));
        assertTrue(containsKind(frame, FakeThin3D.Command.Kind.BATCH_END));
        assertTrue(containsKind(frame, FakeThin3D.Command.Kind.CAMERA_UPDATE));

        game.dispose();
        assertTrue(thin3d.batchDisposed());
        assertTrue(thin3d.modelsDisposed());
    }

    @Test
    void updateMovesBallWhenKeyDownViaBagInput() {
        FakeEngineServices services = new FakeEngineServices();
        services.graphics().setSize(800, 600);
        RollballController controller = new RollballController();
        float startX = controller.ballX();
        RollballGame game = new RollballGame(controller);
        game.createScene();
        game.create(services);
        services.input().setKeyDown(Key.D, true);

        game.update(0.1f);

        assertTrue(controller.ballX() > startX);
    }

    private static boolean containsKind(List<FakeThin3D.Command> commands, FakeThin3D.Command.Kind kind) {
        for (FakeThin3D.Command command : commands) {
            if (command.kind == kind) {
                return true;
            }
        }
        return false;
    }
}
