package io.github.worldsmiths.android;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AndroidConfigTest {

    @Test
    void defaultsMatchLibGdxAndroidApplicationConfiguration() {
        AndroidConfig config = new AndroidConfig();

        assertTrue(config.isUseImmersiveMode());
        assertTrue(config.isUseAccelerometer());
        assertFalse(config.isUseGyroscope());
        assertTrue(config.isUseCompass());
        assertFalse(config.isUseWakelock());
    }

    @Test
    void storesExplicitSensorAndImmersiveFlags() {
        AndroidConfig config = new AndroidConfig(false, false, true, false, true);

        assertFalse(config.isUseImmersiveMode());
        assertFalse(config.isUseAccelerometer());
        assertTrue(config.isUseGyroscope());
        assertFalse(config.isUseCompass());
        assertTrue(config.isUseWakelock());
    }
}
