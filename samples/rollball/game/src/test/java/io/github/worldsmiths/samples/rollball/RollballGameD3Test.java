package io.github.worldsmiths.samples.rollball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.d3.FakeD3;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Headless create/render coverage via {@link FakeD3} — no GL / display required.
 */
class RollballGameD3Test {

    @Test
    void createAndRenderRecordPrimitivesBatchAndTitleWithoutGl() {
        FakeEngineServices services = new FakeEngineServices();
        services.graphics().setSize(800, 600);
        RollballGame game = new RollballGame();
        game.create(services);

        assertNotNull(services.scenes().current());
        assertEquals("Rollball — score: 0", services.graphics().lastTitle());

        FakeD3 d3 = services.d3();
        List<FakeD3.Command> created = d3.commands();
        assertTrue(containsKind(created, FakeD3.Command.Kind.CREATE_SPHERE));
        assertTrue(containsKind(created, FakeD3.Command.Kind.CREATE_BOX));
        assertTrue(containsKind(created, FakeD3.Command.Kind.CREATE_BATCH));
        assertTrue(containsKind(created, FakeD3.Command.Kind.AMBIENT_LIGHT));
        assertTrue(containsKind(created, FakeD3.Command.Kind.DIRECTIONAL_LIGHT));
        assertTrue(containsKind(created, FakeD3.Command.Kind.CREATE_CAMERA));

        d3.clear();
        game.render();

        List<FakeD3.Command> frame = d3.commands();
        assertTrue(containsKind(frame, FakeD3.Command.Kind.BATCH_BEGIN));
        assertTrue(containsKind(frame, FakeD3.Command.Kind.BATCH_RENDER));
        assertTrue(containsKind(frame, FakeD3.Command.Kind.BATCH_END));
        assertTrue(containsKind(frame, FakeD3.Command.Kind.CAMERA_UPDATE));

        game.dispose();
        assertTrue(d3.batchDisposed());
        assertTrue(d3.modelsDisposed());
    }

    @Test
    void updateMovesBallWhenKeyDownViaBagInput() {
        FakeEngineServices services = new FakeEngineServices();
        services.graphics().setSize(800, 600);
        RollballController controller = new RollballController();
        float startX = controller.ballX();
        RollballGame game = new RollballGame(controller);
        game.create(services);
        services.input().setKeyDown(Key.D, true);

        game.update(0.1f);

        assertTrue(controller.ballX() > startX);
    }

    private static boolean containsKind(List<FakeD3.Command> commands, FakeD3.Command.Kind kind) {
        for (FakeD3.Command command : commands) {
            if (command.kind == kind) {
                return true;
            }
        }
        return false;
    }
}
