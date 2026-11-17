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
 * After Da-coords: happy-path consumers use bare {@code includeBuild} (no
 * {@code dependencySubstitution}). Plugin Maven module ids stay locked, and
 * root settings keep flat {@code :backends-*} paths with {@code projectDir}
 * under {@code backends/<platform>}.
 */
class ModuleCoordinateContractTest {

    @Test
    void pluginMavenIdsMatchEngineModuleCoordinates() {
        assertEquals(
                EngineModuleCoordinates.BACKENDS_DESKTOP_NOTATION, DesktopPlugin.BACKENDS_DESKTOP);
        assertEquals(EngineModuleCoordinates.BACKENDS_WEB_NOTATION, WebPlugin.BACKENDS_WEB);
        assertEquals(
                EngineModuleCoordinates.BACKENDS_ANDROID_NOTATION, AndroidPlugin.BACKENDS_ANDROID);
        assertEquals(EngineModuleCoordinates.BACKENDS_IOS_NOTATION, IosPlugin.BACKENDS_IOS);
        assertEquals(EngineModuleCoordinates.GROUP + ":backends-desktop",
                EngineModuleCoordinates.BACKENDS_DESKTOP);
        assertEquals(EngineModuleCoordinates.GROUP + ":backends-web",
                EngineModuleCoordinates.BACKENDS_WEB);
        assertEquals(EngineModuleCoordinates.GROUP + ":backends-android",
                EngineModuleCoordinates.BACKENDS_ANDROID);
        assertEquals(EngineModuleCoordinates.GROUP + ":backends-ios",
                EngineModuleCoordinates.BACKENDS_IOS);
        assertEquals(EngineModuleCoordinates.GROUP + ":ws-core", EngineModuleCoordinates.WS_CORE);
    }

    @Test
    void rootSettingsUseFlatBackendProjectPaths() throws IOException {
        String settings = readEngine("settings.gradle.kts");
        assertFlatBackendInclude(settings, "desktop");
        assertFlatBackendInclude(settings, "web");
        assertFlatBackendInclude(settings, "android");
        assertFlatBackendInclude(settings, "ios");
        assertFalse(
                settings.contains("include(\"backends:"),
                "expected flat backends-* includes, not nested backends:<platform>:\n" + settings);
        assertFalse(
                settings.contains("project(\":backends:"),
                "expected flat :backends-* project paths, not nested :backends:<platform>:\n"
                        + settings);
    }

    @Test
    void happyPathSamplesUseBareIncludeBuild() throws IOException {
        assertBareIncludeBuild(readEngine("samples/snake/settings.gradle.kts"));
        assertBareIncludeBuild(readEngine("samples/rollball/settings.gradle.kts"));
        assertBareIncludeBuild(readEngine("samples/minimal/settings.gradle.kts"));
        assertBareIncludeBuild(readEngine("fixtures/ios-phase0/settings.gradle.kts"));
    }

    @Test
    void snakeOptsInAndroidTargetWithGoogleMaven() throws IOException {
        String snake = readEngine("samples/snake/settings.gradle.kts");
        assertTrue(snake.contains("google()"), snake);
        assertTrue(snake.contains("android {"), snake);
    }

    @Test
    void rollballOptsInAndroidTargetWithGoogleMaven() throws IOException {
        String rollball = readEngine("samples/rollball/settings.gradle.kts");
        assertTrue(rollball.contains("google()"), rollball);
        assertTrue(rollball.contains("android {"), rollball);
    }

    @Test
    void snakeOptsInIosTarget() throws IOException {
        String snake = readEngine("samples/snake/settings.gradle.kts");
        assertTrue(snake.contains("ios {"), snake);
    }

    @Test
    void rollballOptsInIosTarget() throws IOException {
        String rollball = readEngine("samples/rollball/settings.gradle.kts");
        assertTrue(rollball.contains("ios {"), rollball);
    }

    @Test
    void documentedSnippetUsesBareIncludeBuild() throws IOException {
        String doc = readEngine("docs/includebuild-substitutions.md");
        assertTrue(doc.contains("includeBuild(engineRoot)"), doc);
        assertFalse(
                doc.contains("substitute(module("),
                "happy-path docs must not require substitute(module(...)):\n" + doc);
        assertTrue(doc.contains("backends-desktop"), doc);
        assertTrue(doc.contains("ws-core"), doc);
    }

    private static void assertFlatBackendInclude(String settings, String platform) {
        String include = "include(\"backends-" + platform + "\")";
        String projectDir =
                "project(\":backends-"
                        + platform
                        + "\").projectDir = file(\"backends/"
                        + platform
                        + "\")";
        assertTrue(settings.contains(include), "missing " + include + ":\n" + settings);
        assertTrue(settings.contains(projectDir), "missing " + projectDir + ":\n" + settings);
    }

    private static void assertBareIncludeBuild(String settings) {
        assertTrue(settings.contains("includeBuild("), settings);
        assertFalse(
                settings.contains("dependencySubstitution {"),
                "expected bare includeBuild (no dependencySubstitution block):\n" + settings);
        assertFalse(
                settings.contains("substitute(module("),
                "expected no substitute(module(...)) lines:\n" + settings);
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
