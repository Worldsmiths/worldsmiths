package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Headless AssetManager-backed {@link GdxAssetManagerAssets} via injectable queue seam — no
 * Application / display / GL required.
 */
class GdxAssetManagerAssetsTest {

    @Test
    void missingTextureRequestDoesNotThrowAndReachesFailedWithPathAfterPump() {
        RecordingQueue queue = new RecordingQueue();
        GdxTextures textures = emptyTextures();
        GdxMeshes meshes = emptyMeshes();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, meshes, queue, queue);

        LoadHandle handle = assets.request("textures/missing.png", AssetKind.TEXTURE);
        assertNotNull(handle);
        assertEquals(LoadState.QUEUED, handle.state());
        assertTrue(queue.textureLoadedPaths.contains("textures/missing.png"));

        assets.pump();

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().contains("textures/missing.png"));
        assertEquals(1, queue.updateCalls.get());
    }

    @Test
    void completeMergesIntoSameCacheSoResolveDoesNotReopen() {
        final GdxTextures.LoadedTexture stub = stubLoadedTexture();
        RecordingQueue queue = new RecordingQueue();
        queue.textureCatalog.put("textures/heavy.png", stub);
        AtomicInteger opens = new AtomicInteger();
        GdxTextures textures = texturesWithOpener(opens);
        GdxMeshes meshes = emptyMeshes();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, meshes, queue, queue);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertTrue(textures.isAsyncInFlight("textures/heavy.png"));
        assets.pump();

        assertEquals(LoadState.COMPLETE, handle.state());
        assertEquals(1f, handle.progress(), 1e-5f);
        assertFalse(textures.isAsyncInFlight("textures/heavy.png"));
        assertEquals(0, opens.get());
        assertTrue(queue.textureTakeCalls.contains("textures/heavy.png"));

        textures.resolve("textures/heavy.png");
        assertSame(stub, textures.requireLoaded("textures/heavy.png"));
        assertEquals(0, opens.get());
    }

    @Test
    void pumpInvokesQueueUpdateOncePerCall() {
        RecordingQueue queue = new RecordingQueue();
        queue.textureCatalog.put("textures/heavy.png", stubLoadedTexture());
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(
                        emptyTextures(), emptyMeshes(), queue, queue);

        assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assets.pump();
        assets.pump();

        assertEquals(2, queue.updateCalls.get());
    }

    @Test
    void meshRequestEnqueuesAndCompletesAfterPump() {
        RecordingQueue queue = new RecordingQueue();
        final GdxMeshes.LoadedMesh stub = stubLoadedMesh();
        queue.meshCatalog.put("models/hero.gltf", stub);
        GdxMeshes meshes = emptyMeshes();
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(emptyTextures(), meshes, queue, queue);

        LoadHandle mesh = assets.request("models/hero.gltf", AssetKind.MESH);
        assertEquals(LoadState.QUEUED, mesh.state());
        assertTrue(queue.meshLoadedPaths.contains("models/hero.gltf"));
        assertTrue(meshes.isAsyncInFlight("models/hero.gltf"));

        assets.pump();

        assertEquals(LoadState.COMPLETE, mesh.state());
        assertSame(stub, meshes.getIfPresent("models/hero.gltf"));
        assertFalse(meshes.isAsyncInFlight("models/hero.gltf"));
        assertTrue(queue.meshTakeCalls.contains("models/hero.gltf"));
    }

    @Test
    void meshInFlightFailsWhenAssetManagerReportsDependencyPathNotMeshPath() {
        RecordingQueue queue = new RecordingQueue();
        queue.meshCatalog.put("meshes/demo.gltf", stubLoadedMesh());
        queue.meshDependencyErrors.put(
                "meshes/demo.gltf", "meshes/demo_basecolor.png");
        GdxMeshes meshes = emptyMeshes();
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(emptyTextures(), meshes, queue, queue);

        LoadHandle mesh = assets.request("meshes/demo.gltf", AssetKind.MESH);
        assertEquals(LoadState.QUEUED, mesh.state());

        assets.pump();

        assertEquals(LoadState.FAILED, mesh.state());
        assertNotNull(mesh.failureMessage());
        assertTrue(
                mesh.failureMessage().contains("meshes/demo_basecolor.png"),
                mesh.failureMessage());
        assertNull(meshes.getIfPresent("meshes/demo.gltf"));
        assertFalse(meshes.isAsyncInFlight("meshes/demo.gltf"));
    }

    @Test
    void textureInFlightFailsWhenAssetManagerReportsDifferentFailurePath() {
        RecordingQueue queue = new RecordingQueue();
        queue.textureCatalog.put("textures/atlas.png", stubLoadedTexture());
        queue.textureDependencyErrors.put(
                "textures/atlas.png", "textures/atlas_base.png");
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(emptyTextures(), emptyMeshes(), queue, queue);

        LoadHandle texture = assets.request("textures/atlas.png", AssetKind.TEXTURE);

        assets.pump();

        assertEquals(LoadState.FAILED, texture.state());
        assertNotNull(texture.failureMessage());
        assertTrue(texture.failureMessage().contains("textures/atlas_base.png"));
    }

    @Test
    void meshMissReachesFailedAfterPumpWithoutThrowOnRequest() {
        RecordingQueue queue = new RecordingQueue();
        GdxMeshes meshes = emptyMeshes();
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(emptyTextures(), meshes, queue, queue);

        LoadHandle mesh = assets.request("models/missing.gltf", AssetKind.MESH);
        assertNotNull(mesh);
        assertEquals(LoadState.QUEUED, mesh.state());
        assertTrue(queue.meshLoadedPaths.contains("models/missing.gltf"));

        assets.pump();

        assertEquals(LoadState.FAILED, mesh.state());
        assertNotNull(mesh.failureMessage());
        assertTrue(mesh.failureMessage().contains("models/missing.gltf"));
        assertNull(meshes.getIfPresent("models/missing.gltf"));
    }

    @Test
    void musicRequestStillUnsupportedWithoutEnqueue() {
        RecordingQueue queue = new RecordingQueue();
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(
                        emptyTextures(), emptyMeshes(), queue, queue);

        LoadHandle music = assets.request("audio/theme.ogg", AssetKind.MUSIC);
        assertEquals(LoadState.FAILED, music.state());
        assertTrue(music.failureMessage().contains("MUSIC"));
        assertTrue(queue.textureLoadedPaths.isEmpty());
        assertTrue(queue.meshLoadedPaths.isEmpty());
    }

    @Test
    void reRequestSharesInFlightAndCompletedHandle() {
        RecordingQueue queue = new RecordingQueue();
        queue.textureCatalog.put("textures/heavy.png", stubLoadedTexture());
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(
                        emptyTextures(), emptyMeshes(), queue, queue);

        LoadHandle first = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        LoadHandle second = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertSame(first, second);
        assertEquals(1, queue.textureLoadedPaths.size());

        assets.pump();
        LoadHandle third = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertSame(first, third);
    }

    @Test
    void reRequestAfterFailedStartsFreshLoadThatCanCompleteIndependently() {
        RecordingQueue queue = new RecordingQueue();
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(
                        emptyTextures(), emptyMeshes(), queue, queue);

        LoadHandle first = assets.request("textures/retry.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.FAILED, first.state());

        queue.textureCatalog.put("textures/retry.png", stubLoadedTexture());
        LoadHandle second = assets.request("textures/retry.png", AssetKind.TEXTURE);

        assertNotSame(first, second);
        assertEquals(LoadState.QUEUED, second.state());
        assertEquals(LoadState.FAILED, first.state());

        assets.pump();
        assertEquals(LoadState.COMPLETE, second.state());
        assertEquals(LoadState.FAILED, first.state());
        assertEquals(2, queue.updateCalls.get());
    }

    @Test
    void requestAfterDisposeReturnsFailedWithoutTouchingQueue() {
        RecordingQueue queue = new RecordingQueue();
        queue.textureCatalog.put("textures/late.png", stubLoadedTexture());
        GdxTextures textures = emptyTextures();
        GdxMeshes meshes = emptyMeshes();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, meshes, queue, queue);

        assets.dispose();

        LoadHandle handle = assets.request("textures/late.png", AssetKind.TEXTURE);

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().toLowerCase().contains("disposed"));
        assertTrue(queue.textureLoadedPaths.isEmpty());
        assertFalse(textures.isAsyncInFlight("textures/late.png"));
        assertNull(textures.getIfPresent("textures/late.png"));
        assertEquals(1, queue.disposeCalls.get());
    }

    @Test
    void disposeFailsInFlightAndDisposesQueueWithoutDoubleTextureDispose() {
        RecordingQueue queue = new RecordingQueue();
        final AtomicInteger textureDisposes = new AtomicInteger();
        GdxTextures.LoadedTexture owned =
                new GdxTextures.LoadedTexture() {
                    @Override
                    public Texture gdxTexture() {
                        return null;
                    }

                    @Override
                    public void dispose() {
                        textureDisposes.incrementAndGet();
                    }
                };
        queue.textureCatalog.put("textures/heavy.png", owned);
        GdxTextures textures = emptyTextures();
        GdxMeshes meshes = emptyMeshes();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, meshes, queue, queue);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.COMPLETE, handle.state());
        assertSame(owned, textures.getIfPresent("textures/heavy.png"));

        assets.dispose();
        textures.dispose();

        assertEquals(1, queue.disposeCalls.get());
        assertEquals(0, textureDisposes.get());
        assertNull(textures.getIfPresent("textures/heavy.png"));
    }

    private static GdxMeshes emptyMeshes() {
        return new GdxMeshes(
                new GdxMeshes.MeshOpener() {
                    @Override
                    public GdxMeshes.LoadedMesh open(String path) {
                        return null;
                    }
                });
    }

    private static GdxTextures emptyTextures() {
        return texturesWithOpener(new AtomicInteger());
    }

    private static GdxTextures texturesWithOpener(final AtomicInteger opens) {
        return new GdxTextures(
                new GdxTextures.TextureOpener() {
                    @Override
                    public GdxTextures.LoadedTexture open(String path) {
                        opens.incrementAndGet();
                        return null;
                    }
                });
    }

    private static GdxTextures.LoadedTexture stubLoadedTexture() {
        return new GdxTextures.LoadedTexture() {
            @Override
            public Texture gdxTexture() {
                return null;
            }

            @Override
            public void dispose() {}
        };
    }

    private static GdxMeshes.LoadedMesh stubLoadedMesh() {
        return new GdxMeshes.LoadedMesh() {
            @Override
            public Model gdxModel() {
                return null;
            }

            @Override
            public void dispose() {}
        };
    }

    /**
     * Fake AssetManager.update seam: one queued path completes or fails per {@link #update()},
     * mirroring one AM step per pump.
     */
    private static final class RecordingQueue
            implements GdxAssetManagerAssets.TextureAssetQueue,
                    GdxAssetManagerAssets.MeshAssetQueue {
        private final Map<String, GdxTextures.LoadedTexture> textureCatalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        private final Map<String, GdxMeshes.LoadedMesh> meshCatalog =
                new HashMap<String, GdxMeshes.LoadedMesh>();
        /** Parent path → dependency path whose AM error should fail the parent load. */
        final Map<String, String> meshDependencyErrors = new HashMap<String, String>();
        final Map<String, String> textureDependencyErrors = new HashMap<String, String>();
        private final LinkedHashMap<String, AssetKind> pending = new LinkedHashMap<String, AssetKind>();
        private final Map<String, GdxTextures.LoadedTexture> texturesLoaded =
                new HashMap<String, GdxTextures.LoadedTexture>();
        private final Map<String, GdxMeshes.LoadedMesh> meshesLoaded =
                new HashMap<String, GdxMeshes.LoadedMesh>();
        private final Set<String> textureLoadedPaths = new HashSet<String>();
        private final Set<String> meshLoadedPaths = new HashSet<String>();
        private final Set<String> textureTakeCalls = new HashSet<String>();
        private final Set<String> meshTakeCalls = new HashSet<String>();
        private final AtomicInteger updateCalls = new AtomicInteger();
        private final AtomicInteger disposeCalls = new AtomicInteger();
        private GdxAssetManagerAssets.QueueErrorListener errorListener;

        @Override
        public void load(String path) {
            textureLoadedPaths.add(path);
            pending.put(path, AssetKind.TEXTURE);
        }

        @Override
        public void loadMesh(String path) {
            meshLoadedPaths.add(path);
            pending.put(path, AssetKind.MESH);
        }

        @Override
        public boolean update() {
            updateCalls.incrementAndGet();
            if (pending.isEmpty()) {
                return true;
            }
            String path = pending.keySet().iterator().next();
            AssetKind kind = pending.remove(path);
            if (kind == AssetKind.TEXTURE) {
                String depFailure = textureDependencyErrors.get(path);
                if (depFailure != null) {
                    if (errorListener != null) {
                        errorListener.error(
                                depFailure, new RuntimeException("missing " + depFailure));
                    }
                    return pending.isEmpty();
                }
                GdxTextures.LoadedTexture tex = textureCatalog.get(path);
                if (tex == null) {
                    if (errorListener != null) {
                        errorListener.error(path, new RuntimeException("missing " + path));
                    }
                    return pending.isEmpty();
                }
                texturesLoaded.put(path, tex);
            } else {
                String depFailure = meshDependencyErrors.get(path);
                if (depFailure != null) {
                    if (errorListener != null) {
                        errorListener.error(
                                depFailure, new RuntimeException("missing " + depFailure));
                    }
                    return pending.isEmpty();
                }
                GdxMeshes.LoadedMesh mesh = meshCatalog.get(path);
                if (mesh == null) {
                    if (errorListener != null) {
                        errorListener.error(path, new RuntimeException("missing " + path));
                    }
                    return pending.isEmpty();
                }
                meshesLoaded.put(path, mesh);
            }
            return pending.isEmpty();
        }

        @Override
        public float getProgress() {
            int total = texturesLoaded.size() + meshesLoaded.size() + pending.size();
            if (total == 0) {
                return 0f;
            }
            return ((float) texturesLoaded.size() + meshesLoaded.size()) / total;
        }

        @Override
        public boolean isLoaded(String path) {
            return texturesLoaded.containsKey(path);
        }

        @Override
        public GdxTextures.LoadedTexture takeOwnership(String path) {
            textureTakeCalls.add(path);
            return texturesLoaded.remove(path);
        }

        @Override
        public boolean isMeshLoaded(String path) {
            return meshesLoaded.containsKey(path);
        }

        @Override
        public GdxMeshes.LoadedMesh takeMeshOwnership(String path) {
            meshTakeCalls.add(path);
            return meshesLoaded.remove(path);
        }

        @Override
        public void setErrorListener(GdxAssetManagerAssets.QueueErrorListener listener) {
            this.errorListener = listener;
        }

        @Override
        public void cancel(String path) {
            pending.remove(path);
            texturesLoaded.remove(path);
            meshesLoaded.remove(path);
        }

        @Override
        public void dispose() {
            disposeCalls.incrementAndGet();
            pending.clear();
            texturesLoaded.clear();
            meshesLoaded.clear();
        }
    }
}