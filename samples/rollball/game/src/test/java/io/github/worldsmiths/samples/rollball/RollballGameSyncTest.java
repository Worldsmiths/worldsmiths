package io.github.worldsmiths.samples.rollball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Transform3D;
import org.junit.jupiter.api.Test;

class RollballGameSyncTest {

    @Test
    void createPlacesBallAndPickupNodesWithTransform3D() {
        RollballController controller = new RollballController();
        RollballGame game = new RollballGame(controller);
        FakeEngineServices services = new FakeEngineServices();
        game.create(services);

        Scene scene = services.scenes().current();
        assertNotNull(scene);

        Node ball = findNode(scene, "ball");
        assertNotNull(ball);
        Transform3D ballTx = ball.get(Transform3D.class);
        assertNotNull(ballTx);
        assertEquals(0f, ballTx.x, 1e-5f);
        assertEquals(RollballController.BALL_RADIUS, ballTx.y, 1e-5f);
        assertEquals(0f, ballTx.z, 1e-5f);

        for (int i = 0; i < RollballController.PICKUP_COUNT; i++) {
            Node pickup = findNode(scene, "pickup-" + i);
            assertNotNull(pickup);
            Transform3D tx = pickup.get(Transform3D.class);
            assertNotNull(tx);
            float[] pos = controller.pickupPosition(i);
            assertEquals(pos[0], tx.x, 1e-5f);
            assertEquals(pos[1], tx.z, 1e-5f);
            assertTrue(tx.visible);
        }
    }

    @Test
    void syncSceneWritesBallAndHidesCollectedPickups() {
        RollballController controller = new RollballController();
        RollballGame game = new RollballGame(controller);
        FakeEngineServices services = new FakeEngineServices();
        game.create(services);
        Scene scene = services.scenes().current();

        float[] first = controller.pickupPosition(0);
        controller.placeBall(first[0], RollballController.BALL_RADIUS, first[1]);
        controller.update(0.016f, 0f, 0f);
        game.syncScene();

        Transform3D ballTx = findNode(scene, "ball").get(Transform3D.class);
        assertEquals(first[0], ballTx.x, 1e-5f);
        assertEquals(first[1], ballTx.z, 1e-5f);
        assertFalse(findNode(scene, "pickup-0").get(Transform3D.class).visible);
        assertTrue(findNode(scene, "pickup-1").get(Transform3D.class).visible);
        assertEquals(1, controller.score());
    }

    @Test
    void formatTitleIncludesScoreAndWinFailSuffix() {
        assertEquals("Rollball — score: 0", RollballGame.formatTitle(0, false, false));
        assertEquals("Rollball — score: 3 — WIN", RollballGame.formatTitle(3, true, false));
        assertEquals("Rollball — score: 2 — FAIL", RollballGame.formatTitle(2, false, true));
    }

    private static Node findNode(Scene scene, String name) {
        for (Node node : scene.nodes) {
            if (name.equals(node.name)) {
                return node;
            }
        }
        return null;
    }
}
