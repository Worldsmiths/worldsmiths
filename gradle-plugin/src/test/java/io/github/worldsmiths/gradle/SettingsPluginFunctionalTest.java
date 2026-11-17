package io.github.worldsmiths.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assumptions;
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
        assertTrue(launcher.contains("new DesktopConfig(\"Starfall\", 1024, 768, true, true, null)"),
                launcher);
        assertFalse(launcher.contains("Object game"), launcher);
        assertFalse(launcher.contains("System.out.println"), launcher);

        String script = read(".worldsmiths/gen/desktop/build.gradle.kts");
        assertTrue(script.contains("title = \"Starfall\""), script);
        assertTrue(script.contains("width = 1024"), script);
        assertTrue(script.contains("height = 768"), script);
        assertTrue(script.contains("vsync = true"), script);
        assertTrue(script.contains("resizable = true"), script);
        assertFalse(script.contains("icon ="), script);

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
        assertTrue(launcher.contains("new DesktopConfig(\"Worldsmiths\", 800, 600, true, true, null)"),
                launcher);
        String script = read(".worldsmiths/gen/desktop/build.gradle.kts");
        assertTrue(script.contains("title = \"Worldsmiths\""), script);
        assertTrue(script.contains("width = 800"), script);
        assertTrue(script.contains("height = 600"), script);
        assertTrue(script.contains("vsync = true"), script);
        assertTrue(script.contains("resizable = true"), script);
        assertFalse(script.contains("icon ="), script);
    }

    @Test
    void desktopCuratedAttrOverridesReachGeneratedScriptAndLauncher() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {\n"
                + "            title.set(\"Starfall\")\n"
                + "            width.set(1024)\n"
                + "            height.set(768)\n"
                + "            vsync.set(false)\n"
                + "            resizable.set(false)\n"
                + "            icon.set(\"icons/app.png\")\n"
                + "        }\n"
                + "    }\n"));

        BuildResult result = runner(":desktop:compileJava").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":desktop:compileJava").getOutcome());
        String script = read(".worldsmiths/gen/desktop/build.gradle.kts");
        assertTrue(script.contains("vsync = false"), script);
        assertTrue(script.contains("resizable = false"), script);
        assertTrue(script.contains("icon = \"icons/app.png\""), script);
        String launcher = readLauncherSource();
        assertTrue(launcher.contains(
                "new DesktopConfig(\"Starfall\", 1024, 768, false, false, \"icons/app.png\")"),
                launcher);
    }

    @Test
    void assetsDirDefaultsToAssetsInGeneratedDesktopScript() throws IOException {
        write("settings.gradle.kts", defaultSettings(""));

        BuildResult result = runner("projects").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":projects").getOutcome());
        String script = read(".worldsmiths/gen/desktop/build.gradle.kts");
        assertTrue(script.contains("assetsDir = \"assets\""), script);
    }

    @Test
    void assetsDirOverrideReachesGeneratedDesktopAndWebScripts() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    assetsDir = \"game-assets\"\n"
                + "    targets {\n"
                + "        desktop {}\n"
                + "        web {}\n"
                + "    }\n"));

        BuildResult result = runner("projects").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":projects").getOutcome());
        assertTrue(read(".worldsmiths/gen/desktop/build.gradle.kts").contains("assetsDir = \"game-assets\""));
        assertTrue(read(".worldsmiths/gen/web/build.gradle.kts").contains("assetsDir = \"game-assets\""));
    }

    @Test
    void assetsDirOverrideReachesGeneratedIosScript() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    assetsDir = \"game-assets\"\n"
                + "    targets {\n"
                + "        desktop {}\n"
                + "        ios {}\n"
                + "    }\n"));

        BuildResult result = runner("projects").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":projects").getOutcome());
        assertTrue(read(".worldsmiths/gen/ios/build.gradle.kts").contains("assetsDir = \"game-assets\""));
    }

    @Test
    void assetsDirOverrideReachesGeneratedAndroidScript() throws IOException {
        write("gradle.properties", "android.useAndroidX=true\nandroid.builtInKotlin=false\n");
        write("settings.gradle.kts", settingsWithGoogle(""
                + "    assetsDir = \"game-assets\"\n"
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {}\n"
                + "    }\n"));

        BuildResult result = runner("projects").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":projects").getOutcome());
        assertTrue(read(".worldsmiths/gen/android/build.gradle.kts").contains("assetsDir = \"game-assets\""));
    }

    @Test
    void emptyAssetsDirDoesNotBreakAndroidConfigure() throws IOException {
        write("gradle.properties", "android.useAndroidX=true\nandroid.builtInKotlin=false\n");
        Files.createDirectories(projectDir.resolve("assets"));
        write("settings.gradle.kts", settingsWithGoogle(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {}\n"
                + "    }\n"));

        BuildResult result = runner(
                "projects",
                ":android:wsGenerateManifest",
                ":android:wsCopyAndroidNatives").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":projects").getOutcome());
        assertEquals(TaskOutcome.SUCCESS, result.task(":android:wsGenerateManifest").getOutcome());
        assertEquals(TaskOutcome.SUCCESS, result.task(":android:wsCopyAndroidNatives").getOutcome());
    }

    @Test
    void missingAssetsDirDoesNotBreakAndroidConfigure() throws IOException {
        write("gradle.properties", "android.useAndroidX=true\nandroid.builtInKotlin=false\n");
        write("settings.gradle.kts", settingsWithGoogle(""
                + "    assetsDir = \"no-such-assets\"\n"
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {}\n"
                + "    }\n"));

        BuildResult result = runner(
                "projects",
                ":android:wsGenerateManifest",
                ":android:wsCopyAndroidNatives").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":projects").getOutcome());
        assertEquals(TaskOutcome.SUCCESS, result.task(":android:wsGenerateManifest").getOutcome());
        assertEquals(TaskOutcome.SUCCESS, result.task(":android:wsCopyAndroidNatives").getOutcome());
    }

    @Test
    void androidCopyNativesExtractsSharedLibraries() throws IOException {
        write("gradle.properties", "android.useAndroidX=true\nandroid.builtInKotlin=false\n");
        write("settings.gradle.kts", settingsWithGoogle(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {}\n"
                + "    }\n"));

        BuildResult result = runner(":android:wsCopyAndroidNatives").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":android:wsCopyAndroidNatives").getOutcome());
        Path jniRoot = projectDir.resolve(".worldsmiths/gen/android/build/ws-jniLibs");
        assertTrue(Files.isDirectory(jniRoot), "missing jniLibs root " + jniRoot);
        try (Stream<Path> sos = Files.walk(jniRoot)) {
            boolean hasSo = sos.anyMatch(path -> path.getFileName().toString().endsWith(".so"));
            assertTrue(hasSo, "expected at least one .so under " + jniRoot);
        }
    }

    @Test
    void androidDebugApkIncludesShipMarkerFromAssetsDirWhenSdkPresent() throws IOException {
        String androidHome = System.getenv("ANDROID_HOME");
        Assumptions.assumeTrue(
                androidHome != null && !androidHome.isBlank() && Files.isDirectory(Path.of(androidHome)),
                "ANDROID_HOME unset; skipping assembleDebug ship-marker assert");

        write("gradle.properties", "android.useAndroidX=true\nandroid.builtInKotlin=false\n");
        write("local.properties", "sdk.dir=" + androidHome.replace('\\', '/') + "\n");
        write("assets/ws-ship-marker.txt", "worldsmiths-ship-marker\n");
        write("settings.gradle.kts", settingsWithGoogle(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {\n"
                + "            title.set(\"Starfall Android\")\n"
                + "            width.set(640)\n"
                + "            height.set(480)\n"
                + "        }\n"
                + "    }\n"));

        BuildResult result = runner(":android:assembleDebug").build();
        assertAssembleDebugOk(result);

        Path apk = findDebugApk(projectDir.resolve(".worldsmiths/gen/android/build/outputs/apk/debug"));
        Set<String> entries = zipEntryNames(apk);
        assertTrue(
                entries.stream().anyMatch(name -> name.endsWith("ws-ship-marker.txt")),
                "debug APK entries: " + entries);
        assertTrue(
                entries.stream().anyMatch(name -> name.endsWith(".so")),
                "debug APK missing natives (.so); entries: " + entries);
    }

    @Test
    void desktopDistZipIncludesMarkerFromAssetsDir() throws IOException {
        write("assets/ws-ship-marker.txt", "worldsmiths-ship-marker\n");

        BuildResult result = runner(":desktop:distZip").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":desktop:distZip").getOutcome());
        Path distZip = findSingleZip(projectDir.resolve(".worldsmiths/gen/desktop/build/distributions"));
        Set<String> entries = zipEntryNames(distZip);
        assertTrue(entries.stream().anyMatch(name -> name.endsWith("ws-ship-marker.txt")),
                "distZip entries: " + entries);
    }

    @Test
    void emptyAssetsDirDoesNotBreakDesktopDistZip() throws IOException {
        Files.createDirectories(projectDir.resolve("assets"));

        BuildResult result = runner(":desktop:distZip").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":desktop:distZip").getOutcome());
    }

    @Test
    void itchWebZipPutsIndexHtmlAtRootAndIncludesMarker() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        web {}\n"
                + "    }\n"));
        write("assets/ws-ship-marker.txt", "worldsmiths-ship-marker\n");
        write(".worldsmiths/gen/web/build/dist/js/webapp/index.html", "<html></html>\n");
        write(".worldsmiths/gen/web/build/dist/js/webapp/ws-ship-marker.txt", "worldsmiths-ship-marker\n");

        BuildResult result = runner(":web:wsItchWebZip", "-x", "gdx_teavm_web_js_build").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":web:wsItchWebZip").getOutcome());
        Path itchZip = projectDir.resolve(".worldsmiths/gen/web/build/distributions/web-itch.zip");
        assertTrue(Files.isRegularFile(itchZip), "missing " + itchZip);
        Set<String> entries = zipEntryNames(itchZip);
        assertTrue(entries.contains("index.html"), "itch zip entries: " + entries);
        assertTrue(entries.stream().anyMatch(name -> name.endsWith("ws-ship-marker.txt")),
                "itch zip entries: " + entries);
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
    void withoutTargetsAndroidDoesNotIncludeAndroidProject() {
        BuildResult result = runner("projects").build();

        assertTrue(result.getOutput().contains("Project ':desktop'"), result.getOutput());
        assertFalse(result.getOutput().contains("Project ':android'"), result.getOutput());
        assertFalse(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/android/build.gradle.kts")));
    }

    @Test
    void withoutTargetsIosDoesNotIncludeIosProject() {
        BuildResult result = runner("projects").build();

        assertTrue(result.getOutput().contains("Project ':desktop'"), result.getOutput());
        assertFalse(result.getOutput().contains("Project ':ios'"), result.getOutput());
        assertFalse(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/ios/build.gradle.kts")));
    }

    @Test
    void withTargetsIosIncludesGeneratedScriptPluginIdAndDelegateAttach() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        ios {\n"
                + "            title.set(\"Starfall iOS\")\n"
                + "            width.set(640)\n"
                + "            height.set(480)\n"
                + "        }\n"
                + "    }\n"));

        BuildResult result = runner(
                "projects",
                ":ios:wsGenerateDelegate",
                ":ios:wsGenerateRobovmConfig").build();

        assertTrue(result.getOutput().contains("Project ':ios'"), result.getOutput());
        assertTrue(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/ios/build.gradle.kts")));

        String script = read(".worldsmiths/gen/ios/build.gradle.kts");
        assertTrue(script.contains("id(\"io.github.worldsmiths.ios\")"), script);
        assertTrue(script.contains("title = \"Starfall iOS\""), script);
        assertTrue(script.contains("width = 640"), script);
        assertTrue(script.contains("height = 480"), script);

        String launcher = read(".worldsmiths/gen/ios/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/ios/IosDelegate.java");
        assertTrue(launcher.contains("WorldsmithsGame game = new com.example.Game()"), launcher);
        assertTrue(launcher.contains("IosBackend.attach"), launcher);
        assertTrue(launcher.contains("IosApplicationHost"), launcher);
        assertTrue(
                launcher.contains("new IosConfig(640, 480)"),
                "DSL width/height must reach IosConfig in the generated Delegate\n" + launcher);
        assertTrue(launcher.contains("IOSApplicationConfiguration"), launcher);
        assertTrue(launcher.contains("new IOSApplication"), launcher);

        String robovmXml = read(".worldsmiths/gen/ios/build/generated/ws-robovm/robovm.xml");
        assertTrue(robovmXml.contains("${app.mainclass}"), robovmXml);
        assertTrue(robovmXml.contains("<os>ios</os>"), robovmXml);
        String robovmProps = read(".worldsmiths/gen/ios/build/generated/ws-robovm/robovm.properties");
        assertTrue(
                robovmProps.contains("app.mainclass=io.github.worldsmiths.generated.ios.IosDelegate"),
                robovmProps);
        assertTrue(robovmProps.contains("app.name=Starfall iOS"), robovmProps);
        assertTrue(Files.isRegularFile(projectDir.resolve(
                ".worldsmiths/gen/ios/build/generated/ws-robovm/Info.plist.xml")));
    }

    @Test
    void emptyTargetsIosBlockStillGeneratesIosModule() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        ios {}\n"
                + "    }\n"));

        BuildResult result = runner("projects", ":ios:wsGenerateDelegate").build();

        assertTrue(result.getOutput().contains("Project ':ios'"), result.getOutput());
        String script = read(".worldsmiths/gen/ios/build.gradle.kts");
        assertTrue(script.contains("id(\"io.github.worldsmiths.ios\")"), script);
        String launcher = read(".worldsmiths/gen/ios/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/ios/IosDelegate.java");
        assertTrue(launcher.contains("IosBackend.attach"), launcher);
        assertTrue(launcher.contains("new IosConfig(800, 600)"), launcher);
    }

    @Test
    void iosRobovmInstallMustNotFailUnderConfigurationCacheWhenMacOsXcodePresent() throws IOException {
        Assumptions.assumeTrue(
                isMacOsWithXcode(),
                "macOS+Xcode required for :ios:robovmInstall; generation asserts still cover Windows");

        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        ios {\n"
                + "            title.set(\"Starfall iOS\")\n"
                + "            width.set(640)\n"
                + "            height.set(480)\n"
                + "        }\n"
                + "    }\n"));
        write("assets/ws-ship-marker.txt", "phase0\n");

        BuildResult result = runner(
                ":ios:robovmInstall",
                "-Probovm.iosSkipSigning=true").build();

        assertRobovmInstallOk(result);
        assertTrue(
                result.getOutput().contains(STORED) || result.getOutput().contains(REUSED),
                "configuration-cache must not fail (reuse not required)\n" + result.getOutput());
    }

    @Test
    void withTargetsAndroidIncludesGeneratedScriptAndPluginId() throws IOException {
        write("gradle.properties", ""
                + "android.useAndroidX=true\n"
                + "android.builtInKotlin=false\n");
        write("settings.gradle.kts", settingsWithGoogle(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {\n"
                + "            title.set(\"Starfall Android\")\n"
                + "            width.set(640)\n"
                + "            height.set(480)\n"
                + "        }\n"
                + "    }\n"));

        BuildResult result = runner(
                "projects",
                ":android:wsGenerateManifest",
                ":android:wsGenerateLauncher").build();

        assertTrue(result.getOutput().contains("Project ':android'"), result.getOutput());
        assertTrue(Files.isRegularFile(projectDir.resolve(".worldsmiths/gen/android/build.gradle.kts")));

        String script = read(".worldsmiths/gen/android/build.gradle.kts");
        assertTrue(script.contains("id(\"io.github.worldsmiths.android\")"), script);
        assertTrue(script.contains("title = \"Starfall Android\""), script);
        assertTrue(script.contains("width = 640"), script);
        assertTrue(script.contains("height = 480"), script);

        String manifest = read(".worldsmiths/gen/android/build/generated/ws-manifest/AndroidManifest.xml");
        assertTrue(manifest.contains("io.github.worldsmiths.generated.android.AndroidLauncher"), manifest);

        String launcher = read(".worldsmiths/gen/android/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/android/AndroidLauncher.java");
        assertTrue(launcher.contains("WorldsmithsGame game = new com.example.Game()"), launcher);
        assertTrue(launcher.contains("AndroidBackend.attach"), launcher);
        assertTrue(launcher.contains("AndroidApplicationHost"), launcher);
        assertTrue(
                launcher.contains("new AndroidConfig(640, 480)"),
                "DSL width/height must reach AndroidConfig in the generated Activity\n" + launcher);
        assertTrue(
                launcher.contains("SOFT_INPUT_STATE_ALWAYS_HIDDEN"),
                "Launcher should keep the soft keyboard hidden for swipe/tap play\n" + launcher);
    }

    @Test
    void androidWithoutGoogleFailsWithClearMessage() throws IOException {
        write("gradle.properties", "android.useAndroidX=true\nandroid.builtInKotlin=false\n");
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {}\n"
                + "    }\n"));

        BuildResult result = runner("projects").buildAndFail();

        assertTrue(result.getOutput().contains("Google Maven"), result.getOutput());
        assertTrue(result.getOutput().contains("google()"), result.getOutput());
    }

    @Test
    void androidAssembleDebugReusesConfigurationCacheWhenSdkPresent() throws IOException {
        String androidHome = System.getenv("ANDROID_HOME");
        Assumptions.assumeTrue(
                androidHome != null && !androidHome.isBlank() && Files.isDirectory(Path.of(androidHome)),
                "ANDROID_HOME unset; skipping assembleDebug");

        write("gradle.properties", "android.useAndroidX=true\nandroid.builtInKotlin=false\n");
        write("local.properties", "sdk.dir=" + androidHome.replace('\\', '/') + "\n");
        write("settings.gradle.kts", settingsWithGoogle(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        android {\n"
                + "            title.set(\"Starfall Android\")\n"
                + "            width.set(640)\n"
                + "            height.set(480)\n"
                + "        }\n"
                + "    }\n"));

        BuildResult first = runner(":android:assembleDebug").build();
        assertAssembleDebugOk(first);
        assertTrue(
                first.getOutput().contains(STORED) || first.getOutput().contains(REUSED),
                first.getOutput());

        // AGP may install missing build-tools on the first run, which invalidates the CC entry once.
        BuildResult second = runner(":android:assembleDebug").build();
        assertAssembleDebugOk(second);
        if (!second.getOutput().contains(REUSED)) {
            BuildResult third = runner(":android:assembleDebug").build();
            assertAssembleDebugOk(third);
            assertTrue(third.getOutput().contains(REUSED), third.getOutput());
        }
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
        assertTrue(script.contains("storagePrefix = \"app\""), script);

        assertTrue(result.getOutput().contains("ws-teavm-reflectionEnabled=false"), result.getOutput());
        assertTrue(result.getOutput().contains("ws-teavm-reflectionDefaults=false"), result.getOutput());
        assertTrue(result.getOutput().contains("gdx_teavm_web_js_build"), result.getOutput());
        assertTrue(result.getOutput().contains("gdx_teavm_web_wasm_build"), result.getOutput());

        String launcher = read(".worldsmiths/gen/web/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/web/WebMain.java");
        assertTrue(launcher.contains("WorldsmithsGame game = new com.example.Game()"), launcher);
        assertTrue(launcher.contains("WebBackend.start"), launcher);
        assertTrue(launcher.contains("new WebConfig(960, 540, \"app\")"), launcher);
        assertFalse(launcher.contains("Starfall Web"), launcher);
    }

    @Test
    void webStoragePrefixDefaultFlowsIntoGeneratedScriptAndLauncher() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        web {}\n"
                + "    }\n"));

        BuildResult result = runner(":web:wsGenerateLauncher").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":web:wsGenerateLauncher").getOutcome());
        String script = read(".worldsmiths/gen/web/build.gradle.kts");
        assertTrue(script.contains("storagePrefix = \"app\""), script);
        String launcher = read(".worldsmiths/gen/web/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/web/WebMain.java");
        assertTrue(launcher.contains("new WebConfig(800, 600, \"app\")"), launcher);
    }

    @Test
    void webStoragePrefixOverrideReachesGeneratedScriptAndLauncher() throws IOException {
        write("settings.gradle.kts", defaultSettings(""
                + "    targets {\n"
                + "        desktop {}\n"
                + "        web {\n"
                + "            width.set(960)\n"
                + "            height.set(540)\n"
                + "            storagePrefix.set(\"mygame_\")\n"
                + "        }\n"
                + "    }\n"));

        BuildResult result = runner(":web:wsGenerateLauncher").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":web:wsGenerateLauncher").getOutcome());
        String script = read(".worldsmiths/gen/web/build.gradle.kts");
        assertTrue(script.contains("storagePrefix = \"mygame_\""), script);
        String launcher = read(".worldsmiths/gen/web/build/generated/sources/ws-launcher/"
                + "io/github/worldsmiths/generated/web/WebMain.java");
        assertTrue(launcher.contains("new WebConfig(960, 540, \"mygame_\")"), launcher);
    }

    private static void assertAssembleDebugOk(BuildResult result) {
        TaskOutcome outcome = result.task(":android:assembleDebug").getOutcome();
        assertTrue(
                outcome == TaskOutcome.SUCCESS || outcome == TaskOutcome.UP_TO_DATE,
                "assembleDebug outcome=" + outcome + "\n" + result.getOutput());
    }

    private static void assertRobovmInstallOk(BuildResult result) {
        TaskOutcome outcome = result.task(":ios:robovmInstall").getOutcome();
        assertTrue(
                outcome == TaskOutcome.SUCCESS || outcome == TaskOutcome.UP_TO_DATE,
                "robovmInstall outcome=" + outcome + "\n" + result.getOutput());
    }

    private static boolean isMacOsWithXcode() {
        String os = System.getProperty("os.name");
        if (os == null || !os.toLowerCase(Locale.ROOT).startsWith("mac")) {
            return false;
        }
        try {
            Process process = new ProcessBuilder("xcodebuild", "-version")
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
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
        return settingsWithRepositories("mavenCentral()", targetsBlock);
    }

    private String settingsWithGoogle(String targetsBlock) {
        return settingsWithRepositories("google(); mavenCentral()", targetsBlock);
    }

    private String settingsWithRepositories(String repositories, String targetsBlock) {
        String engineRoot = engineRoot().toString().replace('\\', '/');
        return "plugins { id(\"io.github.worldsmiths.engine\") }\n"
                + "dependencyResolutionManagement { repositories { " + repositories + " } }\n"
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

    private static Path findSingleZip(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".zip"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No zip in " + directory));
        }
    }

    private static Path findDebugApk(Path directory) throws IOException {
        assertTrue(Files.isDirectory(directory), "missing APK directory " + directory);
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".apk"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No apk in " + directory));
        }
    }

    private static Set<String> zipEntryNames(Path zip) throws IOException {
        Set<String> names = new HashSet<>();
        try (InputStream in = Files.newInputStream(zip);
                ZipInputStream zipIn = new ZipInputStream(in)) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                names.add(entry.getName());
            }
        }
        return names;
    }
}
