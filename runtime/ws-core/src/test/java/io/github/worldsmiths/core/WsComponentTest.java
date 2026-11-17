package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class WsComponentTest {

    @Test
    void isSourceRetentionTypeTargetWithEmptyDefaultTag() throws Exception {
        Retention retention = WsComponent.class.getAnnotation(Retention.class);
        Target target = WsComponent.class.getAnnotation(Target.class);

        assertEquals(RetentionPolicy.SOURCE, retention.value());
        assertArrayEquals(new ElementType[] {ElementType.TYPE}, target.value());
        assertEquals("", WsComponent.class.getMethod("value").getDefaultValue());
    }

    @Test
    void transform2DTransform3DAndSpriteAreAnnotatedWsComponent() throws IOException {
        Path core = repoRoot().resolve("runtime").resolve("ws-core").resolve("src").resolve("main").resolve("java")
                .resolve("io").resolve("github").resolve("worldsmiths").resolve("core");
        assertTrue(annotatedWithWsComponent(core.resolve("Transform2D.java")));
        assertTrue(annotatedWithWsComponent(core.resolve("Transform3D.java")));
        assertTrue(annotatedWithWsComponent(core.resolve("Sprite.java")));
    }

    private static boolean annotatedWithWsComponent(Path source) throws IOException {
        String text = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        return text.contains("@WsComponent");
    }

    private static Path repoRoot() {
        Path dir = Paths.get("").toAbsolutePath().normalize();
        while (dir != null) {
            if (Files.isRegularFile(dir.resolve("settings.gradle.kts"))
                    && Files.isDirectory(dir.resolve("runtime"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("could not locate worldsmiths repo root from "
                + Paths.get("").toAbsolutePath());
    }
}
