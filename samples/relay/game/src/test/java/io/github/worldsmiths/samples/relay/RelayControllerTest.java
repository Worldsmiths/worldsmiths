package io.github.worldsmiths.samples.relay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelayControllerTest {

    @Test
    void touchingLeg1AdvancesToLeg2WithoutTerminal() {
        RelayController controller = new RelayController();
        assertEquals(1, controller.currentLeg());
        assertFalse(controller.terminal());

        float[] gate1 = controller.activeCheckpointCenter();
        controller.placeAgent(gate1[0], RelayController.AGENT_RADIUS, gate1[1]);
        controller.update(0.016f, 0f, 0f, 0.5f);

        assertEquals(2, controller.currentLeg());
        assertFalse(controller.terminal());
        assertEquals(RelayController.TerminalKind.RUNNING, controller.terminalKind());
        float[] gate2 = controller.activeCheckpointCenter();
        assertEquals(-3f, gate2[0], 1e-5f);
        assertEquals(0f, gate2[1], 1e-5f);
    }

    @Test
    void touchingLeg2UnderParClearsCourse() {
        RelayController controller = new RelayController();
        advanceToLeg2(controller);

        float[] gate2 = controller.activeCheckpointCenter();
        controller.placeAgent(gate2[0], RelayController.AGENT_RADIUS, gate2[1]);
        controller.update(0.016f, 0f, 0f, 1.0f);

        assertTrue(controller.terminal());
        assertTrue(controller.cleared());
        assertFalse(controller.failed());
        assertEquals(RelayController.TerminalKind.CLEAR, controller.terminalKind());
    }

    @Test
    void elapsedAtOrAboveParFailsBeforeClear() {
        RelayController controller = new RelayController();
        controller.update(0.016f, 0f, 0f, RelayController.RUN_PAR_SECONDS);

        assertTrue(controller.terminal());
        assertTrue(controller.failed());
        assertFalse(controller.cleared());
        assertEquals(RelayController.TerminalKind.FAIL, controller.terminalKind());
    }

    @Test
    void overlappingLastGateAtParFailsInsteadOfClear() {
        RelayController controller = new RelayController();
        advanceToLeg2(controller);
        float[] gate2 = controller.activeCheckpointCenter();
        controller.placeAgent(gate2[0], RelayController.AGENT_RADIUS, gate2[1]);

        controller.update(0.016f, 0f, 0f, RelayController.RUN_PAR_SECONDS);

        assertTrue(controller.failed());
        assertFalse(controller.cleared());
    }

    @Test
    void restartClearsTerminalLegAndAgent() {
        RelayController controller = new RelayController();
        advanceToLeg2(controller);
        float[] gate2 = controller.activeCheckpointCenter();
        controller.placeAgent(gate2[0], RelayController.AGENT_RADIUS, gate2[1]);
        controller.update(0.016f, 0f, 0f, 1.0f);
        assertTrue(controller.cleared());

        controller.restart();

        assertFalse(controller.terminal());
        assertEquals(RelayController.TerminalKind.RUNNING, controller.terminalKind());
        assertEquals(1, controller.currentLeg());
        assertEquals(0f, controller.agentX(), 1e-5f);
        assertEquals(RelayController.AGENT_RADIUS, controller.agentY(), 1e-5f);
        assertEquals(0f, controller.agentZ(), 1e-5f);
        float[] gate1 = controller.activeCheckpointCenter();
        assertEquals(3f, gate1[0], 1e-5f);
    }

    @Test
    void midCourseAfterLeg1StillAllowsMove() {
        RelayController controller = new RelayController();
        advanceToLeg2(controller);
        assertFalse(controller.terminal());
        float x = controller.agentX();

        controller.update(0.2f, -1f, 0f, 1.0f);

        assertTrue(controller.agentX() < x);
        assertFalse(controller.terminal());
    }

    @Test
    void ignoresMoveWhenTerminal() {
        RelayController controller = new RelayController();
        controller.update(0.016f, 0f, 0f, RelayController.RUN_PAR_SECONDS);
        assertTrue(controller.failed());
        float x = controller.agentX();

        controller.update(0.5f, 1f, 0f, RelayController.RUN_PAR_SECONDS + 1f);

        assertEquals(x, controller.agentX(), 1e-5f);
    }

    @Test
    void moveAxesMoveAgentOnGround() {
        RelayController controller = new RelayController();
        controller.update(0.2f, 1f, 0f, 0.2f);
        assertTrue(controller.agentX() > 0.1f);
        assertEquals(RelayController.AGENT_RADIUS, controller.agentY(), 1e-4f);
        assertFalse(controller.terminal());
    }

    @Test
    void overlappingInactiveGateOnLeg1DoesNotAdvance() {
        RelayController controller = new RelayController();
        float[] inactive = controller.inactiveCheckpointCenter();
        controller.placeAgent(inactive[0], RelayController.AGENT_RADIUS, inactive[1]);
        controller.update(0.016f, 0f, 0f, 0.5f);

        assertEquals(1, controller.currentLeg());
        assertFalse(controller.terminal());
    }

    private static void advanceToLeg2(RelayController controller) {
        float[] gate1 = controller.activeCheckpointCenter();
        controller.placeAgent(gate1[0], RelayController.AGENT_RADIUS, gate1[1]);
        controller.update(0.016f, 0f, 0f, 0.5f);
        assertEquals(2, controller.currentLeg());
        assertFalse(controller.terminal());
    }
}
