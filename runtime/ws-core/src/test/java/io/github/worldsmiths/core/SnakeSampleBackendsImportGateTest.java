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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * AC5: {@code samples/snake} game sources must not import {@code com.badlogic.gdx.backends}.
 * Runs under root {@code ./gradlew build} via {@code :runtime:ws-core:test} (no display).
 */
class SnakeSampleBackendsImportGateTest {

    private static final String BANNED = "com.badlogic.gdx.backends";

    @Test
    void snakeSampleJavaSourcesMustNotImportGdxBackends() throws IOException {
        Path snakeRoot = repoRoot().resolve("samples").resolve("snake");
        assertTrue(Files.isDirectory(snakeRoot), "missing samples/snake at " + snakeRoot);

        List<Path> javaFiles = new ArrayList<Path>();
        try (Stream<Path> walk = Files.walk(snakeRoot)) {
            walk.filter(p -> p.toString().endsWith(".java")).forEach(javaFiles::add);
        }
        assertFalse(javaFiles.isEmpty(), "samples/snake has no .java sources under " + snakeRoot);

        List<String> hits = new ArrayList<String>();
        for (Path file : javaFiles) {
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (text.contains(BANNED)) {
                hits.add(snakeRoot.relativize(file).toString());
            }
        }
        if (!hits.isEmpty()) {
            fail("samples/snake must not import " + BANNED + ":\n"
                    + hits.stream().collect(Collectors.joining("\n")));
        }
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
