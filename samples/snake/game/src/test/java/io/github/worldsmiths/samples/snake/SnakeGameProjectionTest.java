package io.github.worldsmiths.samples.snake;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SnakeGameProjectionTest {

    @Test
    void screenOrthoMatrixMatchesPixelSpaceSetToOrtho2D() {
        Matrix4 expected = new Matrix4().setToOrtho2D(0f, 0f, 640f, 480f);

        Matrix4 actual = SnakeGame.screenOrthoMatrix(640f, 480f);

        assertArrayEquals(expected.val, actual.val, 1e-5f);
    }

    @Test
    void formatScoreTitleUsesScoreInWindowTitle() {
        assertEquals("Snake — score: 0", SnakeGame.formatScoreTitle(0));
        assertEquals("Snake — score: 7", SnakeGame.formatScoreTitle(7));
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
