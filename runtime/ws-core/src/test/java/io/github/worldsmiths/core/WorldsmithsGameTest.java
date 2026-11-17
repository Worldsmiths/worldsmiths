package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorldsmithsGameTest {

    @Test
    void stubCanBeConstructedAndLifecycleMethodsInvoked() {
        WorldsmithsGame game = new StubGame();

        assertDoesNotThrow(() -> {
            game.create();
            game.update(1f / 60f);
            game.render();
            game.dispose();
        });
    }

    @Test
    void createSceneReturnsASceneInstance() {
        WorldsmithsGame game = new StubGame();

        Scene scene = game.createScene();

        assertNotNull(scene);
        assertTrue(scene.nodes.isEmpty());
    }

    private static final class StubGame implements WorldsmithsGame {
        @Override
        public Scene createScene() {
            return new Scene();
        }

        @Override
        public void create() {}

        @Override
        public void update(float delta) {}

        @Override
        public void render() {}

        @Override
        public void dispose() {}
    }
}
