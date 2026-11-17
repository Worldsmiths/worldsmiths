package io.github.worldsmiths.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
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
    void applyMapsWidthHeightAndStoragePrefixOntoWebConfig() {
        WebConfig config = new WebConfig(960, 540, "mygame_");

        WebApplicationConfiguration web = WebBackend.toWebConfig(config);

        assertEquals(960, web.width);
        assertEquals(540, web.height);
        assertEquals("mygame_", web.storagePrefix);
    }

    @Test
    void applyUsesDefaultStoragePrefix() {
        WebApplicationConfiguration web = WebBackend.toWebConfig(new WebConfig());

        assertEquals("app", web.storagePrefix);
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

        @Override
        public void dispose() {}
    }
}
