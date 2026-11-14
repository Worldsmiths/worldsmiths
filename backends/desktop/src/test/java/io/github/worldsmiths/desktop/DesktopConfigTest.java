package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DesktopConfigTest {

    @Test
    void defaultsProvideTitleWidthAndHeight() {
        DesktopConfig config = new DesktopConfig();

        assertEquals("Worldsmiths", config.getTitle());
        assertEquals(800, config.getWidth());
        assertEquals(600, config.getHeight());
    }
}
