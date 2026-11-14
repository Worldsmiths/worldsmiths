package io.github.worldsmiths.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WebConfigTest {

    @Test
    void defaultsProvideWidthAndHeight() {
        WebConfig config = new WebConfig();

        assertEquals(800, config.getWidth());
        assertEquals(600, config.getHeight());
    }

    @Test
    void defaultsMatchGdxTeavmStoragePrefix() {
        WebConfig config = new WebConfig();

        assertEquals("app", config.getStoragePrefix());
    }

    @Test
    void storesExplicitWidthAndHeight() {
        WebConfig config = new WebConfig(640, 480);

        assertEquals(640, config.getWidth());
        assertEquals(480, config.getHeight());
        assertEquals("app", config.getStoragePrefix());
    }

    @Test
    void storesExplicitStoragePrefix() {
        WebConfig config = new WebConfig(960, 540, "mygame_");

        assertEquals(960, config.getWidth());
        assertEquals(540, config.getHeight());
        assertEquals("mygame_", config.getStoragePrefix());
    }
}
