package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * AC3: every nested {@code samples/*}/game tree must not import platform backends or
 * {@code *ApplicationConfiguration} types - specifically {@code com.badlogic.gdx.backends},
 * {@code com.github.xpenatan.gdx.teavm.backends}, and any import whose type name contains
 * {@code ApplicationConfiguration}.
 * Runs under root {@code ./gradlew build} via {@code :runtime:ws-core:test} (no display).
 */
class SamplesBackendsImportGateTest {

    private static final List<String> BANNED_PREFIXES = Arrays.asList(
            "com.badlogic.gdx.backends",
            "com.github.xpenatan.gdx.teavm.backends");

    private static final String BANNED_TYPE_FRAGMENT = "ApplicationConfiguration";

    private static final Pattern IMPORT_LINE =
            Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;", Pattern.MULTILINE);

    @Test
    void flagsDesktopGdxBackendsImport() {
        assertTrue(
                hasBannedImport("import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;"),
                "desktop gdx backends must be banned");
    }

    @Test
    void flagsTeavmWebBackendsImport() {
        assertTrue(
                hasBannedImport(
                        "import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;"),
                "teavm web backends must be banned");
    }

    @Test
    void flagsApplicationConfigurationTypeNameInImport() {
        assertTrue(
                hasBannedImport(
                        "import com.example.other.CustomApplicationConfiguration;"),
                "ApplicationConfiguration type names must be banned");
    }

    @Test
    void allowsLibgdxCoreImports() {
        assertFalse(
                hasBannedImport("import com.badlogic.gdx.Gdx;\nimport com.badlogic.gdx.graphics.glutils.ShapeRenderer;"),
                "LibGDX core imports must remain allowed");
    }

    @Test
    void allSampleGameJavaSourcesMustNotImportBannedBackends() throws IOException {
        Path samplesRoot = repoRoot().resolve("samples");
        assertTrue(Files.isDirectory(samplesRoot), "missing samples/ at " + samplesRoot);

        List<Path> gameTrees = listSampleGameTrees(samplesRoot);
        assertFalse(gameTrees.isEmpty(), "no samples/*/game trees under " + samplesRoot);

        // D8: gate must cover snake + rollball (and any future samples/*/game).
        List<String> sampleNames = gameTrees.stream()
                .map(p -> p.getParent().getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
        assertTrue(
                sampleNames.contains("snake"),
                "expected samples/snake/game among scanned trees, got " + sampleNames);
        assertTrue(
                sampleNames.contains("rollball"),
                "expected samples/rollball/game among scanned trees, got " + sampleNames);

        List<String> hits = new ArrayList<String>();
        for (Path gameSources : gameTrees) {
            Path sampleRoot = gameSources.getParent();
            List<Path> javaFiles = new ArrayList<Path>();
            try (Stream<Path> walk = Files.walk(gameSources)) {
                walk.filter(p -> p.toString().endsWith(".java")).forEach(javaFiles::add);
            }
            assertFalse(
                    javaFiles.isEmpty(),
                    sampleRoot.getFileName() + "/game has no .java sources under " + gameSources);

            for (Path file : javaFiles) {
                String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                if (hasBannedImport(text)) {
                    hits.add(samplesRoot.relativize(file).toString());
                }
            }
        }
        if (!hits.isEmpty()) {
            fail("samples/*/game sources must not import banned backend / ApplicationConfiguration types:\n"
                    + hits.stream().collect(Collectors.joining("\n")));
        }
    }

    /** Package-visible for focused ban assertions above. */
    static boolean hasBannedImport(String source) {
        Matcher matcher = IMPORT_LINE.matcher(source);
        while (matcher.find()) {
            String imported = matcher.group(1);
            for (String prefix : BANNED_PREFIXES) {
                if (imported.equals(prefix) || imported.startsWith(prefix + ".")) {
                    return true;
                }
            }
            String simpleName = imported.substring(imported.lastIndexOf('.') + 1);
            if (simpleName.contains(BANNED_TYPE_FRAGMENT)) {
                return true;
            }
        }
        return false;
    }

    private static List<Path> listSampleGameTrees(Path samplesRoot) throws IOException {
        List<Path> gameTrees = new ArrayList<Path>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(samplesRoot)) {
            for (Path sampleDir : stream) {
                if (!Files.isDirectory(sampleDir)) {
                    continue;
                }
                Path gameSources = sampleDir.resolve("game");
                if (Files.isDirectory(gameSources)) {
                    gameTrees.add(gameSources);
                }
            }
        }
        return gameTrees;
    }

    private static Path repoRoot() {
        Path dir = Paths.get("").toAbsolutePath().normalize();
        while (dir != null) {
            if (Files.isRegularFile(dir.resolve("settings.gradle.kts"))
                    && Files.isDirectory(dir.resolve("samples"))
                    && Files.isDirectory(dir.resolve("runtime"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("could not locate worldsmiths repo root from "
                + Paths.get("").toAbsolutePath());
    }
}
