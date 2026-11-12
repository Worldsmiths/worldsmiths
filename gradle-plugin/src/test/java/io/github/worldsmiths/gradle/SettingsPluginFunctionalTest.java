package io.github.worldsmiths.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsPluginFunctionalTest {

    private static final String REUSED = "Reusing configuration cache.";
    private static final String STORED = "Configuration cache entry stored.";

    @TempDir
    Path projectDir;

    @BeforeEach
    void createGameProject() throws IOException {
        write("settings.gradle.kts",
                "plugins { id(\"io.github.worldsmiths.engine\") }\n"
                        + "dependencyResolutionManagement { repositories { mavenCentral() } }\n"
                        + "rootProject.name = \"starfall\"\n"
                        + "include(\":game\")\n"
                        + "worldsmiths {\n"
                        + "    gameModule = \":game\"\n"
                        + "    gameClass = \"com.example.Game\"\n"
                        + "}\n");
        write("game/build.gradle.kts",
                "plugins { `java-library` }\n"
                        + "tasks.withType<JavaCompile>().configureEach {\n"
                        + "    options.release.set(8)\n"
                        + "    options.compilerArgs.add(\"-Xlint:-options\")\n"
                        + "}\n"
                        // implementation, not api: the desktop module must still get it at runtime (FR-B04).
                        + "dependencies { implementation(\"org.apache.commons:commons-lang3:3.17.0\") }\n");
        writeGameClass("hello from game");
    }

    @Test
    void desktopRunsGameWithFullClasspathAndReusesConfigurationCache() {
        BuildResult first = runner(":desktop:run", ":desktop:wsPrintRuntimeClasspath").build();

        assertEquals(TaskOutcome.SUCCESS, first.task(":desktop:run").getOutcome());
        assertTrue(first.getOutput().contains(STORED), first.getOutput());
        assertTrue(first.getOutput().contains("Worldsmiths desktop launched: Hello from game"), first.getOutput());
        assertTrue(first.getOutput().contains("ws-classpath: game.jar"), first.getOutput());
        assertTrue(first.getOutput().contains("ws-classpath: commons-lang3-3.17.0.jar"), first.getOutput());
        assertTrue(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/desktop/build.gradle.kts")));

        BuildResult second = runner(":desktop:run", ":desktop:wsPrintRuntimeClasspath").build();

        assertTrue(second.getOutput().contains(REUSED), second.getOutput());
        assertTrue(second.getOutput().contains("Worldsmiths desktop launched: Hello from game"), second.getOutput());
        assertTrue(second.getOutput().contains("ws-classpath: commons-lang3-3.17.0.jar"), second.getOutput());
    }

    @Test
    void editingGameSourcesKeepsTheConfigurationCache() throws IOException {
        runner(":desktop:run").build();
        writeGameClass("edited game");

        BuildResult second = runner(":desktop:run").build();

        assertTrue(second.getOutput().contains(REUSED), second.getOutput());
        assertTrue(second.getOutput().contains("Worldsmiths desktop launched: Edited game"), second.getOutput());
    }

    @Test
    void deletedGeneratedModuleIsRestoredOnCacheHit() throws IOException {
        runner(":desktop:run").build();
        deleteRecursively(projectDir.resolve(".worldsmiths"));

        BuildResult second = runner(":desktop:run").build();

        assertTrue(second.getOutput().contains(REUSED), second.getOutput());
        assertTrue(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/desktop/build.gradle.kts")));
    }

    @Test
    void retiredSettingsPluginIdIsRejected() throws IOException {
        String settings = new String(Files.readAllBytes(projectDir.resolve("settings.gradle.kts")),
                StandardCharsets.UTF_8);
        write("settings.gradle.kts", settings.replace(
                "io.github.worldsmiths.engine", "io.github.worldsmiths.settings"));

        BuildResult result = runner("help").buildAndFail();

        assertTrue(result.getOutput().contains(
                "Plugin [id: 'io.github.worldsmiths.settings'] was not found"), result.getOutput());
    }

    @Test
    void configuringAGameDoesNotAddAPublishTask() {
        BuildResult result = runner("tasks", "--all").build();

        assertFalse(result.getOutput().contains("publish"), result.getOutput());
    }

    @Test
    void gameCompiledAboveJava8FailsConfiguration() throws IOException {
        rewriteGameRelease(17);

        BuildResult result = runner("help").buildAndFail();

        assertTrue(result.getOutput().contains("worldsmiths.gameModule ':game' compiles for Java 17"),
                result.getOutput());
        assertTrue(result.getOutput().contains("Worldsmiths requires Java 8"), result.getOutput());
    }

    @Test
    void raisingTheGameReleaseIsNotHiddenByTheConfigurationCache() throws IOException {
        runner("help").build();
        rewriteGameRelease(17);

        BuildResult result = runner("help").buildAndFail();

        assertTrue(result.getOutput().contains("worldsmiths.gameModule ':game' compiles for Java 17"),
                result.getOutput());
        assertFalse(result.getOutput().contains(REUSED), result.getOutput());
    }

    @Test
    void coreModuleCompiledForJava8Configures() throws IOException {
        useModuleEvaluatedBeforeDesktop();

        BuildResult result = runner("help").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":help").getOutcome());
        assertFalse(result.getOutput().contains("already evaluated"), result.getOutput());
    }

    @Test
    void coreModuleCompiledAboveJava8FailsConfiguration() throws IOException {
        useModuleEvaluatedBeforeDesktop();
        rewriteRelease("core/build.gradle.kts", 17);

        BuildResult result = runner("help").buildAndFail();

        assertTrue(result.getOutput().contains("worldsmiths.gameModule ':core' compiles for Java 17"),
                result.getOutput());
        assertFalse(result.getOutput().contains("already evaluated"), result.getOutput());
    }

    @Test
    void coreModuleTargetCompatibilityAboveJava8FailsConfiguration() throws IOException {
        useModuleEvaluatedBeforeDesktop();
        write("core/build.gradle.kts",
                "plugins { `java-library` }\n"
                        + "java { targetCompatibility = JavaVersion.VERSION_17 }\n"
                        + "dependencies { implementation(\"org.apache.commons:commons-lang3:3.17.0\") }\n");

        BuildResult result = runner("help").buildAndFail();

        assertTrue(result.getOutput().contains("worldsmiths.gameModule ':core' compiles for Java 17"),
                result.getOutput());
    }

    @Test
    void gameModuleWithoutJavaIsNotChecked() throws IOException {
        write("game/build.gradle.kts", "");

        BuildResult result = runner("help").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":help").getOutcome());
    }

    @Test
    void unknownGameModuleFailsWithClearMessage() throws IOException {
        String settings = new String(Files.readAllBytes(projectDir.resolve("settings.gradle.kts")),
                StandardCharsets.UTF_8);
        write("settings.gradle.kts", settings.replace("gameModule = \":game\"", "gameModule = \":nope\""));

        BuildResult result = runner("help").buildAndFail();

        assertTrue(result.getOutput().contains("worldsmiths.gameModule ':nope' is not included"), result.getOutput());
    }

    private GradleRunner runner(String... tasks) {
        String[] args = new String[tasks.length + 2];
        System.arraycopy(tasks, 0, args, 0, tasks.length);
        args[tasks.length] = "--configuration-cache";
        args[tasks.length + 1] = "--stacktrace";
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(args)
                .forwardOutput();
    }

    private void useModuleEvaluatedBeforeDesktop() throws IOException {
        String settings = new String(Files.readAllBytes(projectDir.resolve("settings.gradle.kts")),
                StandardCharsets.UTF_8);
        write("settings.gradle.kts", settings.replace(":game", ":core"));
        String build = new String(Files.readAllBytes(projectDir.resolve("game/build.gradle.kts")),
                StandardCharsets.UTF_8);
        write("core/build.gradle.kts", build);
        String source = new String(Files.readAllBytes(
                projectDir.resolve("game/src/main/java/com/example/Game.java")), StandardCharsets.UTF_8);
        write("core/src/main/java/com/example/Game.java", source);
    }

    private void rewriteGameRelease(int release) throws IOException {
        rewriteRelease("game/build.gradle.kts", release);
    }

    private void rewriteRelease(String buildFile, int release) throws IOException {
        String build = new String(Files.readAllBytes(projectDir.resolve(buildFile)), StandardCharsets.UTF_8);
        write(buildFile, build.replace("options.release.set(8)", "options.release.set(" + release + ")"));
    }

    private void writeGameClass(String message) throws IOException {
        write("game/src/main/java/com/example/Game.java",
                "package com.example;\n"
                        + "import org.apache.commons.lang3.StringUtils;\n"
                        + "public class Game {\n"
                        + "    @Override public String toString() { return StringUtils.capitalize(\"" + message + "\"); }\n"
                        + "}\n");
    }

    private static void deleteRecursively(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }
}
