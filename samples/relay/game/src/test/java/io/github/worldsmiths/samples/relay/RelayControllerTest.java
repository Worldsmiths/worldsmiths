package io.github.worldsmiths.samples.relay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelayControllerTest {

    @Test
    void overlappingActiveCheckpointCompletesTheLeg() {
        RelayController controller = new RelayController();
        assertFalse(controller.legComplete());

        float[] gate = controller.activeCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        controller.update(0.016f, 0f, 0f);

        assertTrue(controller.legComplete());
    }

    @Test
    void overlappingInactiveSecondGateDoesNotCompleteTheLeg() {
        RelayController controller = new RelayController();
        float[] gate = controller.inactiveCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        controller.update(0.016f, 0f, 0f);

        assertFalse(controller.legComplete());
    }

    @Test
    void restartClearsLegCompleteAndRestoresAgent() {
        RelayController controller = new RelayController();
        float[] gate = controller.activeCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        controller.update(0.016f, 0f, 0f);
        assertTrue(controller.legComplete());

        controller.restart();

        assertFalse(controller.legComplete());
        assertEquals(0f, controller.agentX(), 1e-5f);
        assertEquals(RelayController.AGENT_RADIUS, controller.agentY(), 1e-5f);
        assertEquals(0f, controller.agentZ(), 1e-5f);
    }

    @Test
    void moveAxesMoveAgentOnGround() {
        RelayController controller = new RelayController();
        // Short step so we stay clear of the active gate AABB at x=3.
        controller.update(0.2f, 1f, 0f);
        assertTrue(controller.agentX() > 0.1f);
        assertEquals(RelayController.AGENT_RADIUS, controller.agentY(), 1e-4f);
        assertFalse(controller.legComplete());
    }

    @Test
    void ignoresMoveWhenLegComplete() {
        RelayController controller = new RelayController();
        float[] gate = controller.activeCheckpointCenter();
        controller.placeAgent(gate[0], RelayController.AGENT_RADIUS, gate[1]);
        controller.update(0.016f, 0f, 0f);
        assertTrue(controller.legComplete());
        float x = controller.agentX();

        controller.update(0.5f, -1f, 0f);

        assertEquals(x, controller.agentX(), 1e-5f);
    }
}
