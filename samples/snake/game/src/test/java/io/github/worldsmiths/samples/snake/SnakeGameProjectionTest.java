package io.github.worldsmiths.samples.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeDraw2D;
import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.Node;
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
        game.createScene();
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
        game.createScene();
        game.create(services);
        assertEquals("Snake — score: 0", services.graphics().lastTitle());
    }

    @Test
    void updateTurnsUpWhenKeyDownViaBagInput() {
        FakeEngineServices services = new FakeEngineServices();
        SnakeController controller = new SnakeController(5, 5, 20, 15);
        SnakeGame game = new SnakeGame(controller);
        game.createScene();
        game.create(services);
        services.input().setKeyDown(Key.UP, true);

        game.update(SnakeController.STEP_SECONDS);

        assertEquals(5, controller.x());
        assertEquals(6, controller.y());
    }

    @Test
    void createSceneIncludesHeadAndFoodNodesWithDistinctColors() {
        SnakeController controller = new SnakeController(5, 5, 20, 15, scriptedCoords(8, 3));
        SnakeGame game = new SnakeGame(controller);

        Scene scene = game.createScene();

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
        Scene scene = game.createScene();
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
        Scene scene = game.createScene();
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
