package io.github.worldsmiths.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Locks D6 Android toolchain pins (AGP / SDK levels / gdx-backend-android) so catalog and
 * gradle-plugin classpath stay aligned with {@link AndroidSdkPins}.
 */
class AndroidPinContractTest {

    @Test
    void sdkPinsMatchLibGdx1142Toolchain() {
        assertEquals(21, AndroidSdkPins.MIN_SDK);
        assertEquals(36, AndroidSdkPins.COMPILE_SDK);
        assertEquals(36, AndroidSdkPins.TARGET_SDK);
        assertEquals("9.0.0", AndroidSdkPins.AGP_VERSION);
        assertEquals("1.14.2", AndroidSdkPins.GDX_VERSION);
        assertEquals(
                "com.badlogicgames.gdx:gdx-backend-android:1.14.2",
                AndroidSdkPins.GDX_BACKEND_ANDROID);
        assertEquals(
                "com.badlogicgames.gdx:gdx-platform:1.14.2",
                AndroidSdkPins.GDX_PLATFORM);
    }

    @Test
    void versionCatalogRecordsAndroidPins() throws IOException {
        String catalog = readEngine("gradle/libs.versions.toml");
        assertTrue(catalog.contains("agp = \"9.0.0\""), catalog);
        assertTrue(catalog.contains("android-min-sdk = \"21\""), catalog);
        assertTrue(catalog.contains("android-compile-sdk = \"36\""), catalog);
        assertTrue(catalog.contains("android-target-sdk = \"36\""), catalog);
        assertTrue(
                catalog.contains("gdx = \"" + AndroidSdkPins.GDX_VERSION + "\""),
                "catalog gdx must match AndroidSdkPins.GDX_VERSION\n" + catalog);
        assertTrue(
                catalog.contains(
                        "gdx-backend-android = { module = \"com.badlogicgames.gdx:gdx-backend-android\", version.ref = \"gdx\" }"),
                catalog);
        assertTrue(
                catalog.contains(
                        "gdx-platform = { module = \"com.badlogicgames.gdx:gdx-platform\", version.ref = \"gdx\" }"),
                catalog);
    }

    @Test
    void androidPluginUsesSdkPinGdxNotations() throws IOException {
        String plugin =
                readEngine("gradle-plugin/src/main/java/io/github/worldsmiths/gradle/AndroidPlugin.java");
        assertTrue(plugin.contains("AndroidSdkPins.GDX_BACKEND_ANDROID"), plugin);
        assertTrue(plugin.contains("AndroidSdkPins.GDX_PLATFORM"), plugin);
        assertFalse(
                plugin.contains("static final String GDX_VERSION"),
                "GDX_VERSION must live on AndroidSdkPins, not AndroidPlugin\n" + plugin);
    }

    @Test
    void gradlePluginClasspathPinsAgpTeavmStyle() throws IOException {
        String build = readEngine("gradle-plugin/build.gradle.kts");
        assertTrue(
                build.contains("com.android.tools.build:gradle:" + AndroidSdkPins.AGP_VERSION),
                build);
        String settings = readEngine("gradle-plugin/settings.gradle.kts");
        assertTrue(settings.contains("google()"), settings);
    }

    @Test
    void androidPluginLocksJava8ViaAgpCompileOptionsNotRelease() throws IOException {
        String androidPlugin =
                readEngine("gradle-plugin/src/main/java/io/github/worldsmiths/gradle/AndroidPlugin.java");
        String desktopPlugin =
                readEngine("gradle-plugin/src/main/java/io/github/worldsmiths/gradle/DesktopPlugin.java");
        assertTrue(desktopPlugin.contains("getRelease().set(8)"), desktopPlugin);
        assertTrue(
                androidPlugin.contains("setSourceCompatibility(JavaVersion.VERSION_1_8)"),
                androidPlugin);
        assertTrue(
                androidPlugin.contains("setTargetCompatibility(JavaVersion.VERSION_1_8)"),
                androidPlugin);
        // AGP rejects --release on JavaCompile (Android bootclasspath); do not mirror DesktopPlugin.
        assertFalse(
                androidPlugin.contains("getRelease().set("),
                "AGP forbids JavaCompile --release; use compileOptions 1.8 instead\n" + androidPlugin);
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
