package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DesktopLauncherTest {

    @Test
    void labelComesFromTheEngineGroup() {
        assertEquals("io.github.worldsmiths", DesktopLauncher.label());
    }

    @Test
    void doesNotShipLibGdx() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("com.badlogic.gdx.Application"));
    }

    @Test
    void mainDoesNotOpenAWindow() {
        DesktopLauncher.main(new String[0]);
    }
}
