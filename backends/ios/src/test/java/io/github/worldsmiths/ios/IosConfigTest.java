package io.github.worldsmiths.ios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IosConfigTest {

    @Test
    void defaultsMatchLibGdxIosApplicationConfiguration() {
        IosConfig config = new IosConfig();

        assertEquals(IosConfig.DEFAULT_WIDTH, config.getWidth());
        assertEquals(IosConfig.DEFAULT_HEIGHT, config.getHeight());
        assertTrue(config.isPreventScreenDimming());
        assertTrue(config.isOrientationPortrait());
        assertTrue(config.isOrientationLandscape());
        assertTrue(config.isUseAccelerometer());
        assertTrue(config.isUseCompass());
        assertFalse(config.isHideHomeIndicator());
    }

    @Test
    void storesCuratedWidthAndHeight() {
        IosConfig config = new IosConfig(640, 480);

        assertEquals(640, config.getWidth());
        assertEquals(480, config.getHeight());
        assertTrue(config.isPreventScreenDimming());
        assertTrue(config.isOrientationPortrait());
        assertTrue(config.isOrientationLandscape());
        assertTrue(config.isUseAccelerometer());
        assertTrue(config.isUseCompass());
        assertFalse(config.isHideHomeIndicator());
    }

    @Test
    void storesExplicitOrientationAndChromeFlags() {
        IosConfig config = new IosConfig(false, false, true, false, true);

        assertEquals(IosConfig.DEFAULT_WIDTH, config.getWidth());
        assertEquals(IosConfig.DEFAULT_HEIGHT, config.getHeight());
        assertFalse(config.isPreventScreenDimming());
        assertFalse(config.isOrientationPortrait());
        assertTrue(config.isOrientationLandscape());
        assertFalse(config.isUseAccelerometer());
        assertTrue(config.isUseCompass());
        assertTrue(config.isHideHomeIndicator());
    }
}
