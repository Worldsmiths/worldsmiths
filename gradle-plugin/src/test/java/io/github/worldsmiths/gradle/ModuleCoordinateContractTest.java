package io.github.worldsmiths.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Locks plugin Maven module ids to the {@code dependencySubstitution} strings used by
 * samples and TestKit. Project rename / {@code archivesName} are out of scope (Da).
 */
class ModuleCoordinateContractTest {

    @Test
    void sampleSubstitutesMatchPluginModuleCoordinates() throws IOException {
        String desktop = moduleId(DesktopPlugin.BACKENDS_DESKTOP);
        String web = moduleId(WebPlugin.BACKENDS_WEB);
        String android = moduleId(EngineModuleCoordinates.BACKENDS_ANDROID);
        String wsCore = moduleId(EngineModuleCoordinates.WS_CORE);

        String snake = readEngine("samples/snake/settings.gradle.kts");
        assertContainsSubstitute(snake, desktop);
        assertContainsSubstitute(snake, web);
        assertContainsSubstitute(snake, android);
        assertContainsSubstitute(snake, wsCore);

        String minimal = readEngine("samples/minimal/settings.gradle.kts");
        assertContainsSubstitute(minimal, desktop);
        assertContainsSubstitute(minimal, wsCore);
    }

    @Test
    void snakeOptsInAndroidTargetWithGoogleMaven() throws IOException {
        String snake = readEngine("samples/snake/settings.gradle.kts");
        assertTrue(snake.contains("google()"), snake);
        assertTrue(snake.contains("android {"), snake);
        assertTrue(snake.contains(":backends:android"), snake);
    }

    @Test
    void testKitSubstitutesMatchPluginModuleCoordinates() {
        String block = IncludeBuildSubstitutions.dependencySubstitutionKotlin();
        assertContainsSubstitute(block, moduleId(DesktopPlugin.BACKENDS_DESKTOP));
        assertContainsSubstitute(block, moduleId(WebPlugin.BACKENDS_WEB));
        assertContainsSubstitute(block, moduleId(EngineModuleCoordinates.BACKENDS_ANDROID));
        assertContainsSubstitute(block, moduleId(EngineModuleCoordinates.WS_CORE));
        assertTrue(block.contains(":backends:android"), block);
    }

    @Test
    void androidSubstituteLineMatchesPluginModuleCoordinate() {
        assertContainsSubstitute(
                IncludeBuildSubstitutions.backendsAndroidSubstituteKotlin(),
                moduleId(EngineModuleCoordinates.BACKENDS_ANDROID));
        assertTrue(
                IncludeBuildSubstitutions.backendsAndroidSubstituteKotlin()
                        .contains(":backends:android"));
        assertTrue(
                IncludeBuildSubstitutions.dependencySubstitutionKotlin()
                        .contains(IncludeBuildSubstitutions.backendsAndroidSubstituteKotlin()));
    }

    @Test
    void iosSubstituteLineMatchesPluginModuleCoordinate() {
        // Live TestKit block omits ios until :backends:ios exists (T2); the
        // dedicated line still locks the Maven id to the documented substitute.
        assertContainsSubstitute(
                IncludeBuildSubstitutions.backendsIosSubstituteKotlin(),
                moduleId(EngineModuleCoordinates.BACKENDS_IOS));
        assertTrue(
                IncludeBuildSubstitutions.backendsIosSubstituteKotlin()
                        .contains(":backends:ios"));
        assertFalse(
                IncludeBuildSubstitutions.dependencySubstitutionKotlin()
                        .contains(IncludeBuildSubstitutions.backendsIosSubstituteKotlin()),
                "live TestKit substitute must omit ios until :backends:ios exists (T2)");
    }

    @Test
    void documentedSnippetMatchesPluginModuleCoordinates() throws IOException {
        String doc = readEngine("docs/includebuild-substitutions.md");
        assertContainsSubstitute(doc, moduleId(DesktopPlugin.BACKENDS_DESKTOP));
        assertContainsSubstitute(doc, moduleId(WebPlugin.BACKENDS_WEB));
        assertContainsSubstitute(doc, moduleId(EngineModuleCoordinates.BACKENDS_ANDROID));
        assertContainsSubstitute(doc, moduleId(EngineModuleCoordinates.BACKENDS_IOS));
        assertContainsSubstitute(doc, moduleId(EngineModuleCoordinates.WS_CORE));
        assertTrue(doc.contains(":backends:desktop"), doc);
        assertTrue(doc.contains(":backends:web"), doc);
        assertTrue(doc.contains(":backends:android"), doc);
        assertTrue(doc.contains(":backends:ios"), doc);
        assertTrue(doc.contains(":runtime:ws-core"), doc);
    }

    private static void assertContainsSubstitute(String source, String moduleId) {
        String needle = "substitute(module(\"" + moduleId + "\"))";
        assertTrue(source.contains(needle),
                "expected " + needle + " in source, got:\n" + source);
    }

    /** {@code group:name:version} → {@code group:name} (Gradle substitute module id). */
    private static String moduleId(String mavenNotation) {
        int lastColon = mavenNotation.lastIndexOf(':');
        if (lastColon <= 0 || mavenNotation.indexOf(':') == lastColon) {
            return mavenNotation;
        }
        return mavenNotation.substring(0, lastColon);
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
