package io.github.worldsmiths.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;
import org.junit.jupiter.api.Test;

class WebBackendTest {

    @Test
    void startRejectsNullGame() {
        assertThrows(
                NullPointerException.class, () -> WebBackend.start(null, new WebConfig()));
    }

    @Test
    void startRejectsNullConfig() {
        assertThrows(
                NullPointerException.class, () -> WebBackend.start(new StubGame(), null));
    }

    @Test
    void shipsGdxTeavmWebBackendOnTheClasspath() {
        assertDoesNotThrow(
                () ->
                        Class.forName(
                                "com.github.xpenatan.gdx.teavm.backends.web.WebApplication"));
        assertDoesNotThrow(
                () ->
                        Class.forName(
                                "com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration"));
    }

    private static final class StubGame implements WorldsmithsGame {
        @Override
        public Scene createScene() {
            return new Scene("stub");
        }

        @Override
        public void create() {}

        @Override
        public void update(float delta) {}

        @Override
        public void render() {}
    }
}
