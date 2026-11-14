package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class DesktopBackendTest {

    @Test
    void applyMapsTitleSizeVsyncResizableAndIconOntoLwjglConfig() throws Exception {
        DesktopConfig config = new DesktopConfig("Starfall", 1024, 768, false, false, "icons/app.png");

        Lwjgl3ApplicationConfiguration lwjgl = DesktopBackend.toLwjglConfig(config);

        assertEquals("Starfall", field(lwjgl, "title"));
        assertEquals(1024, field(lwjgl, "windowWidth"));
        assertEquals(768, field(lwjgl, "windowHeight"));
        assertFalse((boolean) field(lwjgl, "vSyncEnabled"));
        assertFalse((boolean) field(lwjgl, "windowResizable"));
        assertArrayEquals(new String[] {"icons/app.png"}, (String[]) field(lwjgl, "windowIconPaths"));
    }

    @Test
    void applyOmitsWindowIconWhenIconIsUnset() throws Exception {
        DesktopConfig config = new DesktopConfig();

        Lwjgl3ApplicationConfiguration lwjgl = DesktopBackend.toLwjglConfig(config);

        assertTrue((boolean) field(lwjgl, "vSyncEnabled"));
        assertTrue((boolean) field(lwjgl, "windowResizable"));
        assertNull(field(lwjgl, "windowIconPaths"));
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getSuperclass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
