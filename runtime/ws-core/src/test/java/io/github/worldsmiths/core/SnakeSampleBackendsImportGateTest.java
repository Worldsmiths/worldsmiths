package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
 * AC1: {@code samples/snake} game sources must not import platform backends or
 * {@code *ApplicationConfiguration} types — specifically {@code com.badlogic.gdx.backends},
 * {@code com.github.xpenatan.gdx.teavm.backends}, and any import whose type name contains
 * {@code ApplicationConfiguration}.
 * Runs under root {@code ./gradlew build} via {@code :runtime:ws-core:test} (no display).
 */
class SnakeSampleBackendsImportGateTest {

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
    void snakeSampleJavaSourcesMustNotImportBannedBackends() throws IOException {
        Path snakeRoot = repoRoot().resolve("samples").resolve("snake");
        // Gate the author-owned :game tree only — generated :desktop/:web/:android/:ios launchers
        // under .worldsmiths/gen intentionally import platform backends.
        Path gameSources = snakeRoot.resolve("game");
        assertTrue(Files.isDirectory(gameSources), "missing samples/snake/game at " + gameSources);

        List<Path> javaFiles = new ArrayList<Path>();
        try (Stream<Path> walk = Files.walk(gameSources)) {
            walk.filter(p -> p.toString().endsWith(".java")).forEach(javaFiles::add);
        }
        assertFalse(javaFiles.isEmpty(), "samples/snake/game has no .java sources under " + gameSources);

        List<String> hits = new ArrayList<String>();
        for (Path file : javaFiles) {
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (hasBannedImport(text)) {
                hits.add(snakeRoot.relativize(file).toString());
            }
        }
        if (!hits.isEmpty()) {
            fail("samples/snake game sources must not import banned backend / ApplicationConfiguration types:\n"
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
