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
    void storesExplicitWidthAndHeight() {
        WebConfig config = new WebConfig(640, 480);

        assertEquals(640, config.getWidth());
        assertEquals(480, config.getHeight());
    }
}
