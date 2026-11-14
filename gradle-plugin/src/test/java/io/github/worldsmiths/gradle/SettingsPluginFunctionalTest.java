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
    private static final String ENGINE_VERSION = EngineModuleCoordinates.VERSION;

    @TempDir
    Path projectDir;

    @BeforeEach
    void createGameProject() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {\n"
                + "            title.set(\"Starfall\")\n"
                + "            width.set(1024)\n"
                + "            height.set(768)\n"
                + "        }\n"
                + "    }\n"));
        write("game/build.gradle.kts", gameBuildScript());
        writeGameClass("hello from game");
    }

    @Test
    void desktopCompilesTypedLauncherWithFullClasspathAndReusesConfigurationCache() throws IOException {
        BuildResult first = runner(":desktop:compileJava", ":desktop:wsPrintRuntimeClasspath").build();

        assertEquals(TaskOutcome.SUCCESS, first.task(":desktop:compileJava").getOutcome());
        assertTrue(first.getOutput().contains(STORED), first.getOutput());
        assertTrue(first.getOutput().contains("ws-classpath: game.jar"), first.getOutput());
        assertTrue(first.getOutput().contains("ws-classpath: commons-lang3-3.17.0.jar"), first.getOutput());
        assertTrue(first.getOutput().contains("ws-classpath: ws-core"), first.getOutput());
        assertTrue(first.getOutput().contains("ws-classpath: gdx-backend-lwjgl3"), first.getOutput());
        assertTrue(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/desktop/build.gradle.kts")));

        String launcher = readLauncherSource();
        assertTrue(launcher.contains("WorldsmithsGame game = new com.example.Game()"), launcher);
        assertTrue(launcher.contains("DesktopBackend.start"), launcher);
        assertTrue(launcher.contains("new DesktopConfig(\"Starfall\", 1024, 768)"), launcher);
        assertFalse(launcher.contains("Object game"), launcher);
        assertFalse(launcher.contains("System.out.println"), launcher);

        String script = read(".worldsmiths/gen/desktop/build.gradle.kts");
        assertTrue(script.contains("title = \"Starfall\""), script);
        assertTrue(script.contains("width = 1024"), script);
        assertTrue(script.contains("height = 768"), script);

        BuildResult second = runner(":desktop:compileJava", ":desktop:wsPrintRuntimeClasspath").build();

        assertTrue(second.getOutput().contains(REUSED), second.getOutput());
        assertTrue(second.getOutput().contains("ws-classpath: commons-lang3-3.17.0.jar"), second.getOutput());
    }

    @Test
    void desktopTargetDefaultsFlowIntoGeneratedLauncher() throws IOException {
        write("settings.gradle.kts", defaultSettings(""));

        BuildResult result = runner(":desktop:compileJava").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":desktop:compileJava").getOutcome());
        String launcher = readLauncherSource();
        assertTrue(launcher.contains("new DesktopConfig(\"Worldsmiths\", 800, 600)"), launcher);
        String script = read(".worldsmiths/gen/desktop/build.gradle.kts");
        assertTrue(script.contains("title = \"Worldsmiths\""), script);
        assertTrue(script.contains("width = 800"), script);
        assertTrue(script.contains("height = 600"), script);
    }

    @Test
    void editingGameSourcesKeepsTheConfigurationCache() throws IOException {
        runner(":game:compileJava", ":desktop:compileJava").build();
        writeGameClass("edited game");

        BuildResult second = runner(":game:compileJava", ":desktop:compileJava").build();

        assertTrue(second.getOutput().contains(REUSED), second.getOutput());
        assertEquals(TaskOutcome.SUCCESS, second.task(":game:compileJava").getOutcome());
    }

    @Test
    void deletedGeneratedModuleIsRestoredOnCacheHit() throws IOException {
        runner(":desktop:compileJava").build();
        deleteRecursively(projectDir.resolve(".worldsmiths"));

        BuildResult second = runner(":desktop:compileJava").build();

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
                        + "dependencies {\n"
                        + "    implementation(\"io.github.worldsmiths:ws-core:" + ENGINE_VERSION + "\")\n"
                        + "    implementation(\"org.apache.commons:commons-lang3:3.17.0\")\n"
                        + "}\n");

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

    @Test
    void withoutTargetsWebDoesNotIncludeWebProject() {
        BuildResult result = runner("projects").build();

        assertTrue(result.getOutput().contains("Project ':desktop'"), result.getOutput());
        assertFalse(result.getOutput().contains("Project ':web'"), result.getOutput());
        assertFalse(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/web/build.gradle.kts")));
    }

    @Test
    void withTargetsWebIncludesWebProjectWithReflectionOff() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {\n"
                + "            title.set(\"Starfall\")\n"
                + "            width.set(1024)\n"
                + "            height.set(768)\n"
                + "        }\n"
                + "        web {\n"
                + "            title.set(\"Starfall Web\")\n"
                + "            width.set(960)\n"
                + "            height.set(540)\n"
                + "        }\n"
                + "    }\n"));

        BuildResult result = runner(
                "projects",
                ":web:wsGenerateLauncher",
                ":web:wsPrintTeaVmFlags",
                ":web:tasks",
                "--all").build();

        assertTrue(result.getOutput().contains("Project ':web'"), result.getOutput());
        assertTrue(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/web/build.gradle.kts")));

        String script = read(".worldsmiths/gen/web/build.gradle.kts");
        assertTrue(script.contains("id(\"io.github.worldsmiths.web\")"), script);
        assertTrue(script.contains("title = \"Starfall Web\""), script);
        assertTrue(script.contains("width = 960"), script);
        assertTrue(script.contains("height = 540"), script);

        assertTrue(result.getOutput().contains("ws-teavm-reflectionEnabled=false"), result.getOutput());
        assertTrue(result.getOutput().contains("ws-teavm-reflectionDefaults=false"), result.getOutput());
        assertTrue(result.getOutput().contains("gdx_teavm_web_js_build"), result.getOutput());
        assertTrue(result.getOutput().contains("gdx_teavm_web_wasm_build"), result.getOutput());

        String launcher = read(".worldsmiths/gen/web/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/web/WebMain.java");
        assertTrue(launcher.contains("WorldsmithsGame game = new com.example.Game()"), launcher);
        assertTrue(launcher.contains("WebBackend.start"), launcher);
        assertTrue(launcher.contains("new WebConfig(960, 540)"), launcher);
        assertFalse(launcher.contains("Starfall Web"), launcher);
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
                        + "import io.github.worldsmiths.core.Scene;\n"
                        + "import io.github.worldsmiths.core.WorldsmithsGame;\n"
                        + "import org.apache.commons.lang3.StringUtils;\n"
                        + "public class Game implements WorldsmithsGame {\n"
                        + "    @Override public Scene createScene() { return new Scene(); }\n"
                        + "    @Override public void create() {}\n"
                        + "    @Override public void update(float delta) {}\n"
                        + "    @Override public void render() {}\n"
                        + "    @Override public String toString() {\n"
                        + "        return StringUtils.capitalize(\"" + message + "\");\n"
                        + "    }\n"
                        + "}\n");
    }

    private String defaultSettings(String targetsBlock) {
        String engineRoot = engineRoot().toString().replace('\\', '/');
        return "plugins { id(\"io.github.worldsmiths.engine\") }\n"
                + "dependencyResolutionManagement { repositories { mavenCentral() } }\n"
                + "includeBuild(\"" + engineRoot + "\") {\n"
                + IncludeBuildSubstitutions.dependencySubstitutionKotlin()
                + "}\n"
                + "rootProject.name = \"starfall\"\n"
                + "include(\":game\")\n"
                + "worldsmiths {\n"
                + "    gameModule = \":game\"\n"
                + "    gameClass = \"com.example.Game\"\n"
                + targetsBlock
                + "}\n";
    }

    private static String gameBuildScript() {
        return "plugins { `java-library` }\n"
                + "tasks.withType<JavaCompile>().configureEach {\n"
                + "    options.release.set(8)\n"
                + "    options.compilerArgs.add(\"-Xlint:-options\")\n"
                + "}\n"
                // implementation, not api: the desktop module must still get it at runtime (FR-B04).
                + "dependencies {\n"
                + "    implementation(\"io.github.worldsmiths:ws-core:" + ENGINE_VERSION + "\")\n"
                + "    implementation(\"org.apache.commons:commons-lang3:3.17.0\")\n"
                + "}\n";
    }

    private String readLauncherSource() throws IOException {
        return read(".worldsmiths/gen/desktop/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/desktop/DesktopMain.java");
    }

    private String read(String relativePath) throws IOException {
        return new String(Files.readAllBytes(projectDir.resolve(relativePath)), StandardCharsets.UTF_8);
    }

    private static Path engineRoot() {
        String configured = System.getProperty("ws.engineRoot");
        if (configured != null && !configured.isEmpty()) {
            return Path.of(configured);
        }
        return Path.of("..").toAbsolutePath().normalize();
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
