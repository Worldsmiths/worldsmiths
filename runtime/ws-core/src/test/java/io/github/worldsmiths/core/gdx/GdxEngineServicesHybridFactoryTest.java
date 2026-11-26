package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import io.github.worldsmiths.core.Assets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Factory / package-split gates for HLD hybrid assets: JVM AssetManager vs web cooperative. Headless
 * — no Application / GL.
 */
class GdxEngineServicesHybridFactoryTest {

    @Test
    void jvmAssetsTypeDiffersFromCooperativeAssetsType() {
        GdxTextures textures = new GdxTextures(recordingOpener(new AtomicInteger()));
        Assets jvm = GdxJvmEngineServices.newAssetsForTest(textures, idleQueue());
        Assets coop = GdxEngineServices.newCooperativeAssetsForTest(textures);

        assertTrue(
                jvm instanceof GdxAssetManagerAssets,
                "JVM factory must wire AssetManager-backed assets");
        assertTrue(coop instanceof GdxAssets, "cooperative factory must wire GdxAssets");
        assertNotEquals(jvm.getClass(), coop.getClass());
    }

    @Test
    void webReachableFactoryPathDoesNotReferenceAssetManager() throws IOException {
        Path root = repoRoot();
        List<String> hits = new ArrayList<String>();
        Path[] webReachable =
                new Path[] {
                    root.resolve(
                            "runtime/ws-core/src/main/java/io/github/worldsmiths/core/gdx/GdxAssets.java"),
                    root.resolve(
                            "runtime/ws-core/src/main/java/io/github/worldsmiths/core/gdx/GdxEngineServices.java"),
                    root.resolve(
                            "backends/web/src/main/java/io/github/worldsmiths/web/WebGameListener.java"),
                };
        for (Path file : webReachable) {
            assertTrue(Files.isRegularFile(file), "missing " + file);
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (text.contains("AssetManager")
                    || text.contains("AsyncExecutor")
                    || text.contains("GdxAssetManagerAssets")
                    || text.contains("GdxJvmEngineServices")) {
                hits.add(file.toString());
            }
        }
        if (!hits.isEmpty()) {
            fail(
                    "web-reachable sources must not mention AssetManager / AsyncExecutor / AM assets"
                            + " class:\n"
                            + String.join("\n", hits));
        }

        String webListener =
                new String(
                        Files.readAllBytes(
                                root.resolve(
                                        "backends/web/src/main/java/io/github/worldsmiths/web/WebGameListener.java")),
                        StandardCharsets.UTF_8);
        assertTrue(
                webListener.contains("createCooperative()"),
                "WebGameListener must use createCooperative()");
        assertFalse(
                webListener.contains("GdxEngineServices.create()"),
                "WebGameListener must not call GdxEngineServices.create() (JVM AM path)");
    }

    private static GdxTextures.TextureOpener recordingOpener(final AtomicInteger opens) {
        return new GdxTextures.TextureOpener() {
            @Override
            public GdxTextures.LoadedTexture open(String path) {
                opens.incrementAndGet();
                return null;
            }
        };
    }

    private static GdxAssetManagerAssets.TextureAssetQueue idleQueue() {
        return new GdxAssetManagerAssets.TextureAssetQueue() {
            @Override
            public void load(String path) {}

            @Override
            public boolean update() {
                return true;
            }

            @Override
            public float getProgress() {
                return 0f;
            }

            @Override
            public boolean isLoaded(String path) {
                return false;
            }

            @Override
            public GdxTextures.LoadedTexture takeOwnership(String path) {
                return null;
            }

            @Override
            public void setErrorListener(GdxAssetManagerAssets.QueueErrorListener listener) {}

            @Override
            public void cancel(String path) {}

            @Override
            public void dispose() {}
        };
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
        throw new IllegalStateException(
                "could not locate worldsmiths repo root from " + Paths.get("").toAbsolutePath());
    }
}
