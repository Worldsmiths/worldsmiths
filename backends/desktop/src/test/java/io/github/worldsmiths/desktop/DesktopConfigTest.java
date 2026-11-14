package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DesktopConfigTest {

    @Test
    void defaultsProvideTitleWidthAndHeight() {
        DesktopConfig config = new DesktopConfig();

        assertEquals("Worldsmiths", config.getTitle());
        assertEquals(800, config.getWidth());
        assertEquals(600, config.getHeight());
    }

    @Test
    void defaultsEnableVsyncAndResizableWithNoIcon() {
        DesktopConfig config = new DesktopConfig();

        assertTrue(config.isVsync());
        assertTrue(config.isResizable());
        assertNull(config.getIcon());
    }

    @Test
    void storesExplicitVsyncResizableAndIcon() {
        DesktopConfig config = new DesktopConfig("Game", 640, 480, false, false, "icons/app.png");

        assertEquals("Game", config.getTitle());
        assertEquals(640, config.getWidth());
        assertEquals(480, config.getHeight());
        assertEquals(false, config.isVsync());
        assertEquals(false, config.isResizable());
        assertEquals("icons/app.png", config.getIcon());
    }
}
