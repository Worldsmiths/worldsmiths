package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Source-level guard: SceneCodec and generated registry/serializers must not use reflection APIs. */
class SceneCodecNoReflectionTest {
    private static final String[] BANNED = {
        "Class.forName",
        "java.lang.reflect",
        "com.badlogic.gdx.utils.reflect",
        "ClassReflection",
        "getDeclaredField",
        "getDeclaredMethod",
        "newInstance(",
        "ServiceLoader",
    };

    @Test
    void codecAndGeneratedSourcesUseNoReflection() throws IOException {
        List<Path> dirs = new ArrayList<Path>();
        dirs.add(Paths.get("src/main/java/io/github/worldsmiths/core/SceneCodec.java"));
        dirs.add(Paths.get("build/generated/sources/annotationProcessor/java/main"));

        List<String> hits = new ArrayList<String>();
        int scanned = 0;
        for (Path path : dirs) {
            if (Files.isRegularFile(path)) {
                scanned += scanFile(path, hits);
            } else {
                assertTrue(Files.isDirectory(path), "missing " + path.toAbsolutePath());
                try (Stream<Path> files = Files.walk(path)) {
                    for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                        scanned += scanFile(file, hits);
                    }
                }
            }
        }
        assertTrue(scanned >= 3, "scanned only " + scanned + " files");
        assertTrue(hits.isEmpty(), "reflection found:\n" + String.join("\n", hits));
    }

    private static int scanFile(Path file, List<String> hits) throws IOException {
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        for (String banned : BANNED) {
            if (text.contains(banned)) {
                hits.add(file + ": " + banned);
            }
        }
        return 1;
    }
}
