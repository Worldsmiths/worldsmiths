package io.github.worldsmiths.samples.relay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeAudio;
import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.FakeUi;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.thin3d.FakeThin3D;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Headless create/render + stub-loop coverage via {@link FakeThin3D} — no GL / display required.
 */
class RelayGameThin3DTest {

    @Test
    void createAndRenderRecordPrimitivesBatchAndTitleWithoutGl() {
        FakeEngineServices services = seededServices();
        RelayGame game = new RelayGame();
        game.createScene();
        game.create(services);

        assertTrue(services.graphics().lastTitle().contains("running"));

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
    void overlappingActiveGatePublishesLegCompleteTitle() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        float[] gate = controller.activeCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        game.update(0.016f);

        String title = services.graphics().lastTitle();
        assertTrue(title.toLowerCase().contains("leg complete"), title);
        assertTrue(controller.legComplete());
    }

    @Test
    void legCompletePlaysPackagedCueViaFakeAudio() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        FakeAudio audio = services.audio();
        assertTrue(containsAudioKind(audio.commands(), FakeAudio.Command.Kind.CREATE_SOUND));

        float[] gate = controller.activeCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        game.update(0.016f);

        assertTrue(controller.legComplete());
        assertTrue(containsAudioKind(audio.commands(), FakeAudio.Command.Kind.PLAY));

        game.dispose();
        assertTrue(containsAudioKind(audio.commands(), FakeAudio.Command.Kind.DISPOSE));
    }

    @Test
    void restartKeyAfterLegCompleteResetsTitle() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        float[] gate = controller.activeCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        game.update(0.016f);
        assertTrue(controller.legComplete());

        services.input().setKeyDown(Key.R, true);
        game.update(0.016f);

        assertFalse(controller.legComplete());
        assertTrue(services.graphics().lastTitle().contains("running"));
    }

    @Test
    void updateMovesAgentWhenKeyDownViaFakeInput() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        float startX = controller.agentX();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);
        services.input().setKeyDown(Key.D, true);

        game.update(0.1f);

        assertTrue(controller.agentX() > startX);
    }

    @Test
    void elapsedHudUsesFakeUiCreateDrawAndDispose() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        FakeUi ui = services.ui();
        assertTrue(containsUiKind(ui.commands(), FakeUi.Command.Kind.CREATE_FONT));

        game.update(1.5f);
        game.render();

        assertTrue(containsUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING));
        FakeUi.Command drawn = firstUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING);
        assertTrue(drawn.text != null && drawn.text.contains("1.5"), drawn.text);

        float[] gate = controller.activeCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        game.update(0.016f);
        assertTrue(controller.legComplete());

        services.input().setKeyDown(Key.R, true);
        game.update(0.2f);
        game.render();

        assertFalse(controller.legComplete());
        FakeUi.Command afterRestart = lastUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING);
        assertTrue(
                afterRestart.text != null && afterRestart.text.contains("0.2"),
                afterRestart.text);

        game.dispose();
        assertTrue(containsUiKind(ui.commands(), FakeUi.Command.Kind.DISPOSE));
    }

    private static FakeEngineServices seededServices() {
        FakeEngineServices services = new FakeEngineServices();
        services.graphics().setSize(800, 600);
        services.audio().putSound(RelayGame.LEG_COMPLETE_CUE);
        services.ui().putFont(RelayGame.HUD_FONT);
        return services;
    }

    private static boolean containsKind(
            List<FakeThin3D.Command> commands, FakeThin3D.Command.Kind kind) {
        for (FakeThin3D.Command command : commands) {
            if (command.kind == kind) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAudioKind(
            List<FakeAudio.Command> commands, FakeAudio.Command.Kind kind) {
        for (FakeAudio.Command command : commands) {
            if (command.kind == kind) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsUiKind(List<FakeUi.Command> commands, FakeUi.Command.Kind kind) {
        for (FakeUi.Command command : commands) {
            if (command.kind == kind) {
                return true;
            }
        }
        return false;
    }

    private static FakeUi.Command firstUiKind(
            List<FakeUi.Command> commands, FakeUi.Command.Kind kind) {
        for (FakeUi.Command command : commands) {
            if (command.kind == kind) {
                return command;
            }
        }
        throw new AssertionError("missing FakeUi kind " + kind);
    }

    private static FakeUi.Command lastUiKind(
            List<FakeUi.Command> commands, FakeUi.Command.Kind kind) {
        FakeUi.Command last = null;
        for (FakeUi.Command command : commands) {
            if (command.kind == kind) {
                last = command;
            }
        }
        if (last == null) {
            throw new AssertionError("missing FakeUi kind " + kind);
        }
        return last;
    }
}
