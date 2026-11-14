package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class DesktopLauncherTest {

    @Test
    void labelComesFromTheEngineGroup() {
        assertEquals("io.github.worldsmiths", DesktopLauncher.label());
    }

    @Test
    void shipsLibGdxOnTheClasspath() {
        assertDoesNotThrow(() -> Class.forName("com.badlogic.gdx.Application"));
        assertDoesNotThrow(
                () -> Class.forName("com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application"));
    }
}
