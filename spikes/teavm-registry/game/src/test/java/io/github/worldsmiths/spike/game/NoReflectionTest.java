package io.github.worldsmiths.spike.game;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Source-level guard for the code TeaVM compiles: runtime, game, generated registry and web launcher. */
class NoReflectionTest {
    private static final String[] BANNED = {
        "Class.forName", "java.lang.reflect", "com.badlogic.gdx.utils.reflect", "ClassReflection",
        "getDeclaredField", "getDeclaredMethod", "newInstance(", "ServiceLoader", "teavm-reflection",
        "ReflectionPolicy", "addReflectionClass"
    };

    @Test
    void teavmCompiledSourcesUseNoReflection() throws IOException {
        Path root = Paths.get(System.getProperty("spike.root"));
        List<Path> dirs = new ArrayList<Path>();
        dirs.add(root.resolve("runtime/src/main/java"));
        dirs.add(root.resolve("game/src/main/java"));
        dirs.add(root.resolve("game/build/generated/sources/annotationProcessor/java/main"));
        dirs.add(root.resolve("web/src/main/java"));
        List<String> hits = new ArrayList<String>();
        int scanned = 0;
        for (Path dir : dirs) {
            assertTrue(Files.isDirectory(dir), "missing " + dir);
            try (Stream<Path> files = Files.walk(dir)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                    scanned++;
                    String text = new String(Files.readAllBytes(file), "UTF-8");
                    for (String banned : BANNED) {
                        if (text.contains(banned)) hits.add(root.relativize(file) + ": " + banned);
                    }
                }
            }
        }
        assertTrue(scanned >= 10, "scanned only " + scanned + " files");
        assertTrue(hits.isEmpty(), "reflection found:\n" + String.join("\n", hits));
    }

    @Test
    void webBuildDeclaresNoReflectionPatterns() throws IOException {
        Path root = Paths.get(System.getProperty("spike.root"));
        String build = new String(Files.readAllBytes(root.resolve("web/build.gradle.kts")), "UTF-8");
        assertTrue(build.contains("reflectionEnabled.set(false)"), "gdx-teavm reflection metadata must stay off");
        assertTrue(build.contains("reflectionDefaults.set(false)"), "gdx-teavm reflection defaults must stay off");
        assertTrue(!build.contains("reflection.add") && !build.contains("reflection("), "web build adds reflection patterns");
        try (Stream<Path> files = Files.walk(root.resolve("web/src"))) {
            assertTrue(files.noneMatch(p -> p.getFileName().toString().contains("reflection")), "reflection config file found");
        }
    }
}
