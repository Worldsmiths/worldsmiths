package io.github.worldsmiths.samples.mesh3d;

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

/** Source-level gate: sample game main sources must not import LibGDX or gdx-gltf. */
class Mesh3dGameNoGdxImportTest {

    private static final String[] BANNED = {"com.badlogic.gdx", "net.mgsx.gltf"};

    @Test
    void gameMainSourcesDoNotImportLibGdxOrGltf() throws IOException {
        Path mainJava = Paths.get("src/main/java");
        assertTrue(Files.isDirectory(mainJava), "missing " + mainJava.toAbsolutePath());

        List<String> hits = new ArrayList<String>();
        int scanned = 0;
        try (Stream<Path> files = Files.walk(mainJava)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                scanned += 1;
                String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                for (String banned : BANNED) {
                    if (text.contains(banned)) {
                        hits.add(file + " (" + banned + ")");
                    }
                }
            }
        }
        assertTrue(scanned >= 1, "scanned only " + scanned + " game sources");
        assertTrue(hits.isEmpty(), "banned imports found in:\n" + String.join("\n", hits));
    }
}