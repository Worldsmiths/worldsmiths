package io.github.worldsmiths.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Locks D7 iOS toolchain pins (RoboVM / MetalANGLE / natives-ios) so catalog and
 * gradle-plugin classpath stay aligned with {@link IosPins}.
 */
class IosPinContractTest {

    @Test
    void iosPinsMatchLibGdx1142AndRobovm2327() {
        assertEquals("2.3.27", IosPins.ROBOVM_VERSION);
        assertEquals("1.14.2", IosPins.GDX_VERSION);
        assertEquals(
                "com.badlogicgames.gdx:gdx-backend-robovm-metalangle:1.14.2",
                IosPins.GDX_BACKEND_ROBOVM_METALANGLE);
        assertEquals(
                "com.badlogicgames.gdx:gdx-platform:1.14.2",
                IosPins.GDX_PLATFORM);
        assertEquals("natives-ios", IosPins.NATIVES_IOS_CLASSIFIER);
        assertEquals(
                "com.mobidevelop.robovm:robovm-gradle-plugin:2.3.27",
                IosPins.ROBOVM_GRADLE_PLUGIN);
    }

    @Test
    void versionCatalogRecordsIosPins() throws IOException {
        String catalog = readEngine("gradle/libs.versions.toml");
        assertTrue(catalog.contains("robovm = \"2.3.27\""), catalog);
        assertTrue(
                catalog.contains("gdx = \"" + IosPins.GDX_VERSION + "\""),
                "catalog gdx must match IosPins.GDX_VERSION\n" + catalog);
        assertTrue(
                catalog.contains(
                        "gdx-backend-robovm-metalangle = { module = \"com.badlogicgames.gdx:gdx-backend-robovm-metalangle\", version.ref = \"gdx\" }"),
                catalog);
        assertTrue(
                catalog.contains(
                        "gdx-platform = { module = \"com.badlogicgames.gdx:gdx-platform\", version.ref = \"gdx\" }"),
                catalog);
        assertTrue(
                catalog.contains(
                        "robovm-gradle-plugin = { module = \"com.mobidevelop.robovm:robovm-gradle-plugin\", version.ref = \"robovm\" }"),
                catalog);
    }

    @Test
    void gradlePluginClasspathPinsRobovmTeavmStyle() throws IOException {
        String build = readEngine("gradle-plugin/build.gradle.kts");
        assertTrue(
                build.contains("com.mobidevelop.robovm:robovm-gradle-plugin:" + IosPins.ROBOVM_VERSION),
                build);
    }

    private static String readEngine(String relativePath) throws IOException {
        Path file = engineRoot().resolve(relativePath);
        assertTrue(Files.isRegularFile(file), "missing " + file);
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    private static Path engineRoot() {
        String configured = System.getProperty("ws.engineRoot");
        if (configured != null && !configured.isEmpty()) {
            return Path.of(configured);
        }
        return Path.of("..").toAbsolutePath().normalize();
    }
}
