package io.github.worldsmiths.gradle;

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
        String wsCore = moduleId(EngineModuleCoordinates.WS_CORE);

        String snake = readEngine("samples/snake/settings.gradle.kts");
        assertContainsSubstitute(snake, desktop);
        assertContainsSubstitute(snake, web);
        assertContainsSubstitute(snake, wsCore);

        String minimal = readEngine("samples/minimal/settings.gradle.kts");
        assertContainsSubstitute(minimal, desktop);
        assertContainsSubstitute(minimal, wsCore);
    }

    @Test
    void testKitSubstitutesMatchPluginModuleCoordinates() {
        String block = IncludeBuildSubstitutions.dependencySubstitutionKotlin();
        assertContainsSubstitute(block, moduleId(DesktopPlugin.BACKENDS_DESKTOP));
        assertContainsSubstitute(block, moduleId(WebPlugin.BACKENDS_WEB));
        assertContainsSubstitute(block, moduleId(EngineModuleCoordinates.WS_CORE));
    }

    @Test
    void androidSubstituteLineMatchesPluginModuleCoordinate() {
        // Live TestKit block omits android until :backends:android exists (T2); the
        // dedicated line still locks the Maven id to the documented substitute.
        assertContainsSubstitute(
                IncludeBuildSubstitutions.backendsAndroidSubstituteKotlin(),
                moduleId(EngineModuleCoordinates.BACKENDS_ANDROID));
        assertTrue(
                IncludeBuildSubstitutions.backendsAndroidSubstituteKotlin()
                        .contains(":backends:android"));
    }

    @Test
    void documentedSnippetMatchesPluginModuleCoordinates() throws IOException {
        String doc = readEngine("docs/includebuild-substitutions.md");
        assertContainsSubstitute(doc, moduleId(DesktopPlugin.BACKENDS_DESKTOP));
        assertContainsSubstitute(doc, moduleId(WebPlugin.BACKENDS_WEB));
        assertContainsSubstitute(doc, moduleId(EngineModuleCoordinates.BACKENDS_ANDROID));
        assertContainsSubstitute(doc, moduleId(EngineModuleCoordinates.WS_CORE));
        assertTrue(doc.contains(":backends:desktop"), doc);
        assertTrue(doc.contains(":backends:web"), doc);
        assertTrue(doc.contains(":backends:android"), doc);
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
