package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class WorldsmithsGameTest {

    @Test
    void stubCanBeConstructedAndLifecycleMethodsInvoked() {
        WorldsmithsGame game = new StubGame();
        FakeEngineServices services = new FakeEngineServices();

        assertDoesNotThrow(() -> {
            game.create(services);
            game.update(1f / 60f);
            game.render();
            game.dispose();
        });
    }

    @Test
    void createReceivesNonNullEngineServices() {
        StubGame game = new StubGame();
        FakeEngineServices services = new FakeEngineServices();

        game.create(services);

        assertSame(services, game.receivedServices);
        assertNotNull(game.receivedServices.input());
        assertNotNull(game.receivedServices.clock());
        assertNotNull(game.receivedServices.graphics());
    }

    @Test
    void hasNoCreateSceneMethod() {
        for (Method method : WorldsmithsGame.class.getDeclaredMethods()) {
            assertFalse(
                    "createScene".equals(method.getName()),
                    "WorldsmithsGame must not declare createScene");
        }
    }

    private static final class StubGame implements WorldsmithsGame {
        EngineServices receivedServices;

        @Override
        public void create(EngineServices services) {
            this.receivedServices = services;
        }

        @Override
        public void update(float delta) {}

        @Override
        public void render() {}

        @Override
        public void dispose() {}
    }
}
