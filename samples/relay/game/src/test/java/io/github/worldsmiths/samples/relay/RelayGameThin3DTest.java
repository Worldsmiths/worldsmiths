package io.github.worldsmiths.samples.relay;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void formatElapsedHudExactWhileRunningAndOnTerminals() {
        RelayController controller = new RelayController();
        assertEquals("1.5  1/2", RelayGame.formatElapsedHud(1.5f, controller));

        advanceToLeg2(controller);
        assertEquals("2.0  2/2", RelayGame.formatElapsedHud(2.0f, controller));

        float[] gate2 = controller.activeCheckpointCenter();
        controller.placeAgent(gate2[0], RelayController.AGENT_RADIUS, gate2[1]);
        controller.update(0.016f, 0f, 0f, 2.0f);
        assertEquals("2.0  2/2 CLEAR", RelayGame.formatElapsedHud(2.0f, controller));

        controller.restart();
        controller.update(0.016f, 0f, 0f, RelayController.RUN_PAR_SECONDS);
        assertEquals(
                formatFailHud(RelayController.RUN_PAR_SECONDS),
                RelayGame.formatElapsedHud(RelayController.RUN_PAR_SECONDS, controller));
    }

    @Test
    void formatTitleExactForRunningClearAndFail() {
        assertEquals("Relay — running", RelayGame.formatTitle(RelayController.TerminalKind.RUNNING));
        assertEquals("Relay — CLEAR", RelayGame.formatTitle(RelayController.TerminalKind.CLEAR));
        assertEquals("Relay — FAIL", RelayGame.formatTitle(RelayController.TerminalKind.FAIL));
    }

    @Test
    void clearingCoursePublishesClearTitle() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        clearCourseUnderPar(controller, game);

        String title = services.graphics().lastTitle();
        assertTrue(title.contains("CLEAR"), title);
        assertTrue(controller.cleared());
    }

    @Test
    void clearPlaysPackagedCueViaFakeAudioOnce() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        FakeAudio audio = services.audio();
        assertTrue(containsAudioKind(audio.commands(), FakeAudio.Command.Kind.CREATE_SOUND));
        int playsBefore = countAudioKind(audio.commands(), FakeAudio.Command.Kind.PLAY);

        advanceToLeg2ViaGame(controller, game);
        assertEquals(playsBefore, countAudioKind(audio.commands(), FakeAudio.Command.Kind.PLAY));

        float[] gate2 = controller.activeCheckpointCenter();
        controller.placeAgent(gate2[0], RelayController.AGENT_RADIUS, gate2[1]);
        game.update(0.016f);

        assertTrue(controller.cleared());
        assertEquals(playsBefore + 1, countAudioKind(audio.commands(), FakeAudio.Command.Kind.PLAY));

        game.update(0.016f);
        assertEquals(playsBefore + 1, countAudioKind(audio.commands(), FakeAudio.Command.Kind.PLAY));

        game.dispose();
        assertTrue(containsAudioKind(audio.commands(), FakeAudio.Command.Kind.DISPOSE));
    }

    @Test
    void failDoesNotPlayCue() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        FakeAudio audio = services.audio();
        int playsBefore = countAudioKind(audio.commands(), FakeAudio.Command.Kind.PLAY);

        game.update(RelayController.RUN_PAR_SECONDS);

        assertTrue(controller.failed());
        assertEquals(playsBefore, countAudioKind(audio.commands(), FakeAudio.Command.Kind.PLAY));
    }

    @Test
    void restartKeyAfterClearResetsTitle() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        clearCourseUnderPar(controller, game);
        assertTrue(controller.cleared());

        services.input().setKeyDown(Key.R, true);
        game.update(0.016f);

        assertFalse(controller.terminal());
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
    void midCourseAfterLeg1StillAllowsMove() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        advanceToLeg2ViaGame(controller, game);
        assertFalse(controller.terminal());
        float x = controller.agentX();

        services.input().setKeyDown(Key.A, true);
        game.update(0.1f);

        assertTrue(controller.agentX() < x);
    }

    @Test
    void hudDrawStringShowsLiveMarkerClearFailAndFrozenElapsed() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        FakeUi ui = services.ui();
        assertTrue(containsUiKind(ui.commands(), FakeUi.Command.Kind.CREATE_FONT));

        game.update(1.5f);
        game.render();
        FakeUi.Command running = lastUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING);
        assertTrue(running.text != null && running.text.contains("1.5"), running.text);
        assertTrue(running.text.contains("1/2"), running.text);

        advanceToLeg2ViaGame(controller, game);
        game.render();
        FakeUi.Command leg2 = lastUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING);
        assertTrue(leg2.text != null && leg2.text.contains("2/2"), leg2.text);
        assertFalse(leg2.text.contains("CLEAR"), leg2.text);

        float[] gate2 = controller.activeCheckpointCenter();
        controller.placeAgent(gate2[0], RelayController.AGENT_RADIUS, gate2[1]);
        game.update(0.016f);
        assertTrue(controller.cleared());
        game.render();
        FakeUi.Command clearHud = lastUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING);
        assertTrue(clearHud.text != null && clearHud.text.contains("CLEAR"), clearHud.text);
        assertTrue(clearHud.text.contains("2/2"), clearHud.text);
        float frozen = extractLeadingSeconds(clearHud.text);
        game.update(0.5f);
        game.render();
        FakeUi.Command frozenHud = lastUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING);
        assertTrue(frozenHud.text != null && frozenHud.text.contains("CLEAR"), frozenHud.text);
        assertEquals(frozen, extractLeadingSeconds(frozenHud.text), 1e-5f);

        services.input().setKeyDown(Key.R, true);
        game.update(0.016f);
        services.input().setKeyDown(Key.R, false);
        game.update(RelayController.RUN_PAR_SECONDS);
        game.render();
        FakeUi.Command failHud = lastUiKind(ui.commands(), FakeUi.Command.Kind.DRAW_STRING);
        assertTrue(failHud.text != null && failHud.text.contains("FAIL"), failHud.text);
        assertTrue(failHud.text.contains("1/2"), failHud.text);

        game.dispose();
        assertTrue(containsUiKind(ui.commands(), FakeUi.Command.Kind.DISPOSE));
    }

    @Test
    void afterLeg1AdvanceActiveCenterAndTransformMatchLeg2() {
        FakeEngineServices services = seededServices();
        RelayController controller = new RelayController();
        RelayGame game = new RelayGame(controller);
        game.createScene();
        game.create(services);

        advanceToLeg2ViaGame(controller, game);

        float[] active = controller.activeCheckpointCenter();
        assertEquals(-3f, active[0], 1e-5f);
        assertEquals(0f, active[1], 1e-5f);
        assertEquals(-3f, game.activeGateTransform().x, 1e-5f);
        assertEquals(0f, game.activeGateTransform().z, 1e-5f);
    }

    private static String formatFailHud(float elapsed) {
        RelayController failed = new RelayController();
        failed.update(0.016f, 0f, 0f, elapsed);
        return RelayGame.formatElapsedHud(elapsed, failed);
    }

    private static void advanceToLeg2(RelayController controller) {
        float[] gate1 = controller.activeCheckpointCenter();
        controller.placeAgent(gate1[0], RelayController.AGENT_RADIUS, gate1[1]);
        controller.update(0.016f, 0f, 0f, 0.5f);
        assertEquals(2, controller.currentLeg());
    }

    private static void advanceToLeg2ViaGame(RelayController controller, RelayGame game) {
        float[] gate1 = controller.activeCheckpointCenter();
        controller.placeAgent(gate1[0], RelayController.AGENT_RADIUS, gate1[1]);
        game.update(0.016f);
        assertEquals(2, controller.currentLeg());
        assertFalse(controller.terminal());
    }

    private static void clearCourseUnderPar(RelayController controller, RelayGame game) {
        advanceToLeg2ViaGame(controller, game);
        float[] gate2 = controller.activeCheckpointCenter();
        controller.placeAgent(gate2[0], RelayController.AGENT_RADIUS, gate2[1]);
        game.update(0.016f);
        assertTrue(controller.cleared());
    }

    private static float extractLeadingSeconds(String hud) {
        int space = hud.indexOf(' ');
        return Float.parseFloat(hud.substring(0, space));
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

    private static int countAudioKind(List<FakeAudio.Command> commands, FakeAudio.Command.Kind kind) {
        int n = 0;
        for (FakeAudio.Command command : commands) {
            if (command.kind == kind) {
                n++;
            }
        }
        return n;
    }

    private static boolean containsUiKind(List<FakeUi.Command> commands, FakeUi.Command.Kind kind) {
        for (FakeUi.Command command : commands) {
            if (command.kind == kind) {
                return true;
            }
        }
        return false;
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
