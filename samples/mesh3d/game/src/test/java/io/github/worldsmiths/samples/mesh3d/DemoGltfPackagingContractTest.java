package io.github.worldsmiths.samples.mesh3d;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Packaging contract: ships {@code meshes/demo.gltf} with an external image (not GLB-required) and
 * declares desktop / web / android targets for assetsDir wiring.
 */
class DemoGltfPackagingContractTest {

    private static final String GLTF_RELATIVE = "meshes/demo.gltf";
    private static final String IMAGE_RELATIVE = "meshes/demo_basecolor.png";

    @Test
    void demoGltfAndExternalImageArePackagedForTargets() throws IOException {
        Path sampleRoot = Paths.get("..").toAbsolutePath().normalize();
        Path gltf = sampleRoot.resolve("assets").resolve(GLTF_RELATIVE);
        Path image = sampleRoot.resolve("assets").resolve(IMAGE_RELATIVE);

        assertTrue(Files.isRegularFile(gltf), "missing packaged asset " + gltf);
        assertTrue(Files.isRegularFile(image), "missing external image " + image);

        String gltfText = new String(Files.readAllBytes(gltf), StandardCharsets.UTF_8);
        assertTrue(gltfText.contains("demo_basecolor.png"), "gltf must reference external image");
        assertFalse(gltfText.contains("\"bufferView\"") && gltfText.contains("data:application"),
                "sample should not require embedded GLB; prefer external images");

        Path settings = sampleRoot.resolve("settings.gradle.kts");
        assertTrue(Files.isRegularFile(settings), "missing " + settings);
        String settingsText = new String(Files.readAllBytes(settings), StandardCharsets.UTF_8);
        assertTrue(settingsText.contains("desktop"), "settings must declare desktop target:\n" + settingsText);
        assertTrue(settingsText.contains("web"), "settings must declare web target:\n" + settingsText);
        assertTrue(settingsText.contains("android"), "settings must declare android target:\n" + settingsText);

        for (String target : new String[] {"desktop", "web", "android"}) {
            Path genScript =
                    sampleRoot.resolve(".worldsmiths").resolve("gen").resolve(target).resolve("build.gradle.kts");
            if (Files.isRegularFile(genScript)) {
                String gen = new String(Files.readAllBytes(genScript), StandardCharsets.UTF_8);
                assertTrue(gen.contains("assetsDir"), target + " generated script must wire assetsDir:\n" + gen);
            }
        }
    }
}