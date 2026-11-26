package io.github.worldsmiths.samples.asyncload;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Packaging contract for the async heavy texture: the sample must ship a real
 * {@code assets/textures/heavy.png} (larger than the 141 B sprites assets) and declare
 * desktop / web / android targets so the engine plugin packages {@code assetsDir} on each.
 * Deterministic — does not run TeaVM or APK builds.
 */
class HeavyTexturePackagingContractTest {

    /** Sprites sample coin/hero PNGs are 141 bytes; heavy must be clearly larger. */
    private static final long MIN_HEAVY_BYTES = 142L;

    private static final String HEAVY_RELATIVE = "textures/heavy.png";

    @Test
    void heavyTextureIsPackagedForDesktopWebAndAndroid() throws IOException {
        Path sampleRoot = Paths.get("..").toAbsolutePath().normalize();
        Path heavy = sampleRoot.resolve("assets").resolve(HEAVY_RELATIVE);

        assertTrue(Files.isRegularFile(heavy), "missing packaged asset " + heavy);
        long size = Files.size(heavy);
        assertTrue(
                size > MIN_HEAVY_BYTES,
                "heavy.png must be clearly larger than 141 B sprites assets, was " + size + " B");

        Path settings = sampleRoot.resolve("settings.gradle.kts");
        assertTrue(Files.isRegularFile(settings), "missing " + settings);
        String settingsText = new String(Files.readAllBytes(settings), StandardCharsets.UTF_8);
        assertTrue(settingsText.contains("desktop"), "settings must declare desktop target:\n" + settingsText);
        assertTrue(settingsText.contains("web"), "settings must declare web target:\n" + settingsText);
        assertTrue(settingsText.contains("android"), "settings must declare android target:\n" + settingsText);

        // Lightweight assetsDir wiring check when generated scripts exist from prior settings evaluate.
        for (String target : new String[] {"desktop", "web", "android"}) {
            Path genScript =
                    sampleRoot.resolve(".worldsmiths").resolve("gen").resolve(target).resolve("build.gradle.kts");
            if (Files.isRegularFile(genScript)) {
                String gen = new String(Files.readAllBytes(genScript), StandardCharsets.UTF_8);
                assertTrue(
                        gen.contains("assetsDir"),
                        target + " generated script must wire assetsDir:\n" + gen);
            }
        }

        // Optional: if a prior QA APK is present, it should contain the heavy texture path.
        Path apk =
                sampleRoot
                        .resolve(".worldsmiths")
                        .resolve("gen")
                        .resolve("android")
                        .resolve("build")
                        .resolve("outputs")
                        .resolve("apk")
                        .resolve("debug")
                        .resolve("android-debug.apk");
        if (Files.isRegularFile(apk)) {
            // Soft presence of path string in the zip central directory is enough; no unzip needed.
            byte[] bytes = Files.readAllBytes(apk);
            String asLatin1 = new String(bytes, StandardCharsets.ISO_8859_1);
            assertTrue(
                    asLatin1.contains(HEAVY_RELATIVE) || asLatin1.contains("heavy.png"),
                    "debug APK should package " + HEAVY_RELATIVE + " when present at " + apk);
        }
    }
}
