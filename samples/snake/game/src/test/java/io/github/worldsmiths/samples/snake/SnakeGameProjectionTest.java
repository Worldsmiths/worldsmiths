package io.github.worldsmiths.samples.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.Controls;
import io.github.worldsmiths.core.FakeDraw2D;
import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.PadButton;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneWalk;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SnakeGameProjectionTest {

    @Test
    void cellSizeAliasesPublicSceneWalkConstant() {
        assertEquals(SceneWalk.CELL_SIZE, SnakeGame.CELL_SIZE, 0.001f);
    }

    @Test
    void renderRecordsScreenOrthoAndGridLinesOnFakeDraw2D() {
        FakeEngineServices services = new FakeEngineServices();
        services.graphics().setSize(640, 480);
        SnakeGame game = new SnakeGame();
        game.create(services);

        game.render();

        FakeDraw2D draw2d = services.draw2d();
        assertEquals(0f, draw2d.lastOrthoX(), 1e-5f);
        assertEquals(0f, draw2d.lastOrthoY(), 1e-5f);
        assertEquals(640f, draw2d.lastOrthoWidth(), 1e-5f);
        assertEquals(480f, draw2d.lastOrthoHeight(), 1e-5f);

        List<FakeDraw2D.Command> commands = draw2d.commands();
        assertEquals(FakeDraw2D.Command.Kind.ORTHO, commands.get(0).kind);
        assertEquals(FakeDraw2D.Command.Kind.BEGIN, commands.get(1).kind);
        assertEquals(FakeDraw2D.Command.Kind.COLOR, commands.get(2).kind);
        int lineCount = 0;
        for (FakeDraw2D.Command command : commands) {
            if (command.kind == FakeDraw2D.Command.Kind.LINE) {
                lineCount++;
            }
        }
        // (GRID_WIDTH + 1) vertical + (GRID_HEIGHT + 1) horizontal
        assertEquals((SnakeGame.GRID_WIDTH + 1) + (SnakeGame.GRID_HEIGHT + 1), lineCount);
        assertEquals(FakeDraw2D.Command.Kind.END, commands.get(commands.size() - 1).kind);
    }

    @Test
    void formatScoreTitleUsesScoreInWindowTitle() {
        assertEquals("Snake — score: 0", SnakeGame.formatScoreTitle(0));
        assertEquals("Snake — score: 7", SnakeGame.formatScoreTitle(7));
    }

    @Test
    void createPublishesScoreTitleViaGraphics() {
        FakeEngineServices services = new FakeEngineServices();
        SnakeGame game = new SnakeGame();
        game.create(services);
        assertNotNull(services.scenes().current());
        assertEquals("Snake — score: 0", services.graphics().lastTitle());
    }

    @Test
    void updateTurnsUpWhenKeyDownViaBagInput() {
        FakeEngineServices services = new FakeEngineServices();
        SnakeController controller = new SnakeController(5, 5, 20, 15);
        SnakeGame game = new SnakeGame(controller);
        game.create(services);
        // Shared DeviceState: key Fake drives the bound move_up Control.
        services.input().setKeyDown(Key.UP, true);

        game.update(SnakeController.STEP_SECONDS);

        assertEquals(5, controller.x());
        assertEquals(6, controller.y());
    }

    @Test
    void updateTurnsUpWhenFakePadAloneThroughBindings() {
        FakeEngineServices services = new FakeEngineServices();
        SnakeController controller = new SnakeController(5, 5, 20, 15);
        SnakeGame game = new SnakeGame(controller);
        game.create(services);
        // Keys clear; only pad — dual-device path, not setActionActive.
        assertFalse(services.input().isKeyDown(Key.UP));
        assertFalse(services.input().isKeyDown(Key.W));
        services.actions().setPadButtonDown(PadButton.DPAD_UP, true);

        game.update(SnakeController.STEP_SECONDS);

        assertEquals(5, controller.x());
        assertEquals(6, controller.y());
    }

    @Test
    void remapMoveUpSilencesOldKeyAndFiresNewKey() {
        FakeEngineServices services = new FakeEngineServices();
        SnakeController controller = new SnakeController(5, 5, 20, 15);
        SnakeGame game = new SnakeGame(controller);
        game.create(services);
        // Replace-all via real bind — not setActionActive.
        services.actions().bind("move_up", Controls.key(Key.I));

        services.input().setKeyDown(Key.UP, true);
        game.update(SnakeController.STEP_SECONDS);
        assertEquals(6, controller.x(), "old UP binding must be silent after remap");
        assertEquals(5, controller.y());

        services.input().setKeyDown(Key.UP, false);
        services.input().setKeyDown(Key.I, true);
        game.update(SnakeController.STEP_SECONDS);
        assertEquals(6, controller.x());
        assertEquals(6, controller.y(), "new I binding must fire move_up");
    }

    @Test
    void restartViaFakeKeyThroughRestartAction() {
        FakeEngineServices services = new FakeEngineServices();
        // Food off the rightward path so we die at the wall without filling the grid.
        // Extra pair for placeFood on restart.
        SnakeController controller = new SnakeController(0, 0, 3, 2, scriptedCoords(2, 1, 1, 0));
        SnakeGame game = new SnakeGame(controller);
        game.create(services);
        game.update(SnakeController.STEP_SECONDS);
        game.update(SnakeController.STEP_SECONDS);
        game.update(SnakeController.STEP_SECONDS);
        assertFalse(controller.alive());

        services.input().setKeyDown(Key.R, true);
        game.update(0f);

        assertTrue(controller.alive());
        assertEquals(0, controller.x());
        assertEquals(0, controller.y());
    }

    @Test
    void restartViaFakePadStartThroughRestartAction() {
        FakeEngineServices services = new FakeEngineServices();
        SnakeController controller = new SnakeController(0, 0, 3, 2, scriptedCoords(2, 1, 1, 0));
        SnakeGame game = new SnakeGame(controller);
        game.create(services);
        game.update(SnakeController.STEP_SECONDS);
        game.update(SnakeController.STEP_SECONDS);
        game.update(SnakeController.STEP_SECONDS);
        assertFalse(controller.alive());

        services.actions().setPadButtonDown(PadButton.START, true);
        game.update(0f);

        assertTrue(controller.alive());
        assertEquals(0, controller.x());
        assertEquals(0, controller.y());
    }

    @Test
    void swipeUpViaRawInputStillTurns() {
        FakeEngineServices services = new FakeEngineServices();
        SnakeController controller = new SnakeController(5, 5, 20, 15);
        SnakeGame game = new SnakeGame(controller);
        game.create(services);

        services.input().setJustTouched(true);
        services.input().setTouched(true);
        services.input().setPointer(100, 250);
        game.update(0f);

        services.input().setJustTouched(false);
        services.input().setTouched(false);
        // Screen Y grows downward; finger moves up → smaller Y (past swipe threshold).
        services.input().setPointer(105, 150);
        game.update(SnakeController.STEP_SECONDS);

        assertEquals(5, controller.x());
        assertEquals(6, controller.y());
    }

    @Test
    void createIncludesHeadAndFoodNodesWithDistinctColors() {
        SnakeController controller = new SnakeController(5, 5, 20, 15, scriptedCoords(8, 3));
        SnakeGame game = new SnakeGame(controller);
        FakeEngineServices services = new FakeEngineServices();
        game.create(services);

        Scene scene = services.scenes().current();
        assertNotNull(scene);

        Node head = findNode(scene, "head");
        Node food = findNode(scene, "food");
        assertNotNull(head);
        assertNotNull(food);
        Transform2D headTx = head.get(Transform2D.class);
        Transform2D foodTx = food.get(Transform2D.class);
        Sprite headSprite = head.get(Sprite.class);
        Sprite foodSprite = food.get(Sprite.class);
        assertNotNull(headTx);
        assertNotNull(foodTx);
        assertNotNull(headSprite);
        assertNotNull(foodSprite);
        assertEquals(5f, headTx.x, 1e-5f);
        assertEquals(5f, headTx.y, 1e-5f);
        assertEquals(8f, foodTx.x, 1e-5f);
        assertEquals(3f, foodTx.y, 1e-5f);
        assertTrue(colorsDiffer(headSprite, foodSprite));
    }

    @Test
    void syncSceneAddsBodySegmentNodesWhenLengthGrows() {
        SnakeController controller = new SnakeController(0, 0, 3, 1, scriptedCoords(1, 0, 2, 0));
        SnakeGame game = new SnakeGame(controller);
        FakeEngineServices services = new FakeEngineServices();
        game.create(services);
        Scene scene = services.scenes().current();
        assertEquals(0, countNodesNamedPrefix(scene, "body-"));

        controller.update(SnakeController.STEP_SECONDS);
        game.syncScene();

        assertEquals(1, countNodesNamedPrefix(scene, "body-"));
        Node body1 = findNode(scene, "body-1");
        assertNotNull(body1);
        Transform2D bodyTx = body1.get(Transform2D.class);
        Sprite bodySprite = body1.get(Sprite.class);
        Sprite headSprite = findNode(scene, "head").get(Sprite.class);
        assertEquals(0f, bodyTx.x, 1e-5f);
        assertEquals(0f, bodyTx.y, 1e-5f);
        assertTrue(colorsDiffer(headSprite, bodySprite));
        assertTrue(colorsDiffer(bodySprite, findNode(scene, "food").get(Sprite.class)));
        assertEquals(1f, findNode(scene, "head").get(Transform2D.class).x, 1e-5f);
        assertEquals(2f, findNode(scene, "food").get(Transform2D.class).x, 1e-5f);
    }

    @Test
    void syncSceneRemovesBodySegmentNodesAfterRestart() {
        SnakeController controller = new SnakeController(0, 0, 3, 1, scriptedCoords(1, 0, 2, 0, 1, 0));
        SnakeGame game = new SnakeGame(controller);
        FakeEngineServices services = new FakeEngineServices();
        game.create(services);
        Scene scene = services.scenes().current();
        controller.update(SnakeController.STEP_SECONDS);
        game.syncScene();
        assertEquals(1, countNodesNamedPrefix(scene, "body-"));

        controller.restart();
        game.syncScene();

        assertEquals(0, countNodesNamedPrefix(scene, "body-"));
        assertEquals(0f, findNode(scene, "head").get(Transform2D.class).x, 1e-5f);
        assertEquals(0f, findNode(scene, "head").get(Transform2D.class).y, 1e-5f);
        assertEquals(1f, findNode(scene, "food").get(Transform2D.class).x, 1e-5f);
        assertEquals(0f, findNode(scene, "food").get(Transform2D.class).y, 1e-5f);
    }

    private static Node findNode(Scene scene, String name) {
        for (Node node : scene.nodes) {
            if (name.equals(node.name)) {
                return node;
            }
        }
        return null;
    }

    private static int countNodesNamedPrefix(Scene scene, String prefix) {
        int count = 0;
        for (Node node : scene.nodes) {
            if (node.name != null && node.name.startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }

    private static boolean colorsDiffer(Sprite a, Sprite b) {
        return a.r != b.r || a.g != b.g || a.b != b.b;
    }

    private static Random scriptedCoords(int... coords) {
        return new Random() {
            private int index;

            @Override
            public int nextInt(int bound) {
                return coords[index++];
            }
        };
    }
}
