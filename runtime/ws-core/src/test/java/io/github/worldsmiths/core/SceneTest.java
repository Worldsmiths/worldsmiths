package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SceneTest {

    @Test
    void namedSceneHoldsAddedNodesInOrder() {
        Scene scene = new Scene("root");
        Node a = new Node("a");
        Node b = new Node("b");

        Scene returned = scene.add(a).add(b);

        assertSame(scene, returned);
        assertEquals("root", scene.name);
        assertEquals(Arrays.asList(a, b), scene.nodes);
    }
}
