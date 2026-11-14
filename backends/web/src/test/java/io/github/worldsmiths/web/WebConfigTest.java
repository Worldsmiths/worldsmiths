package io.github.worldsmiths.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WebConfigTest {

    @Test
    void defaultsProvideTitleWidthAndHeight() {
        WebConfig config = new WebConfig();

        assertEquals("Worldsmiths", config.getTitle());
        assertEquals(800, config.getWidth());
        assertEquals(600, config.getHeight());
    }

    @Test
    void rejectsNullTitle() {
        assertThrows(NullPointerException.class, () -> new WebConfig(null, 640, 480));
    }
}
