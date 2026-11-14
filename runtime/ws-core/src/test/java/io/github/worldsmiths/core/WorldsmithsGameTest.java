package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class WorldsmithsGameTest {

    @Test
    void stubCanBeConstructedAndLifecycleMethodsInvoked() {
        WorldsmithsGame game = new StubGame();

        assertDoesNotThrow(() -> {
            game.create();
            game.update(1f / 60f);
            game.render();
        });
    }

    private static final class StubGame implements WorldsmithsGame {
        @Override
        public void create() {}

        @Override
        public void update(float delta) {}

        @Override
        public void render() {}
    }
}
