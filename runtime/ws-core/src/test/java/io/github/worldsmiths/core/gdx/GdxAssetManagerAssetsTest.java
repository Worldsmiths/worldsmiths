package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Texture;
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
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, queue);

        LoadHandle handle = assets.request("textures/missing.png", AssetKind.TEXTURE);
        assertNotNull(handle);
        assertEquals(LoadState.QUEUED, handle.state());
        assertTrue(queue.loadedPaths.contains("textures/missing.png"));

        assets.pump();

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().contains("textures/missing.png"));
        assertEquals(1, queue.updateCalls.get());
    }

    @Test
    void completeMergesIntoSameCacheSoResolveDoesNotReopen() {
        final GdxTextures.LoadedTexture stub = stubLoaded();
        RecordingQueue queue = new RecordingQueue();
        queue.catalog.put("textures/heavy.png", stub);
        AtomicInteger opens = new AtomicInteger();
        GdxTextures textures = texturesWithOpener(opens);
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, queue);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertTrue(textures.isAsyncInFlight("textures/heavy.png"));
        assets.pump();

        assertEquals(LoadState.COMPLETE, handle.state());
        assertEquals(1f, handle.progress(), 1e-5f);
        assertFalse(textures.isAsyncInFlight("textures/heavy.png"));
        assertEquals(0, opens.get());
        assertTrue(queue.takeCalls.contains("textures/heavy.png"));

        textures.resolve("textures/heavy.png");
        assertSame(stub, textures.requireLoaded("textures/heavy.png"));
        assertEquals(0, opens.get());
    }

    @Test
    void pumpInvokesQueueUpdateOncePerCall() {
        RecordingQueue queue = new RecordingQueue();
        queue.catalog.put("textures/heavy.png", stubLoaded());
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(emptyTextures(), queue);

        assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assets.pump();
        assets.pump();

        assertEquals(2, queue.updateCalls.get());
    }

    @Test
    void meshAndMusicRequestReachFailedWithoutEnqueue() {
        RecordingQueue queue = new RecordingQueue();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(emptyTextures(), queue);

        LoadHandle mesh = assets.request("models/hero.mesh", AssetKind.MESH);
        assertEquals(LoadState.FAILED, mesh.state());
        assertTrue(mesh.failureMessage().toLowerCase().contains("unsupported"));
        assertTrue(queue.loadedPaths.isEmpty());

        LoadHandle music = assets.request("audio/theme.ogg", AssetKind.MUSIC);
        assertEquals(LoadState.FAILED, music.state());
        assertTrue(music.failureMessage().contains("MUSIC"));
        assertTrue(queue.loadedPaths.isEmpty());
    }

    @Test
    void reRequestSharesInFlightAndCompletedHandle() {
        RecordingQueue queue = new RecordingQueue();
        queue.catalog.put("textures/heavy.png", stubLoaded());
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(emptyTextures(), queue);

        LoadHandle first = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        LoadHandle second = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertSame(first, second);
        assertEquals(1, queue.loadedPaths.size());

        assets.pump();
        LoadHandle third = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertSame(first, third);
    }

    @Test
    void reRequestAfterFailedStartsFreshLoadThatCanCompleteIndependently() {
        RecordingQueue queue = new RecordingQueue();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(emptyTextures(), queue);

        LoadHandle first = assets.request("textures/retry.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.FAILED, first.state());

        queue.catalog.put("textures/retry.png", stubLoaded());
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
        queue.catalog.put("textures/late.png", stubLoaded());
        GdxTextures textures = emptyTextures();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, queue);

        assets.dispose();

        LoadHandle handle = assets.request("textures/late.png", AssetKind.TEXTURE);

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().toLowerCase().contains("disposed"));
        assertTrue(queue.loadedPaths.isEmpty());
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
        queue.catalog.put("textures/heavy.png", owned);
        GdxTextures textures = emptyTextures();
        GdxAssetManagerAssets assets = new GdxAssetManagerAssets(textures, queue);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.COMPLETE, handle.state());
        assertSame(owned, textures.getIfPresent("textures/heavy.png"));

        assets.dispose();
        textures.dispose();

        assertEquals(1, queue.disposeCalls.get());
        // Ownership stays with the queue/AM — GdxTextures must not dispose the GPU resource again.
        assertEquals(0, textureDisposes.get());
        assertNull(textures.getIfPresent("textures/heavy.png"));
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

    private static GdxTextures.LoadedTexture stubLoaded() {
        return new GdxTextures.LoadedTexture() {
            @Override
            public Texture gdxTexture() {
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
    private static final class RecordingQueue implements GdxAssetManagerAssets.TextureAssetQueue {
        private final Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        private final LinkedHashMap<String, Boolean> pending = new LinkedHashMap<String, Boolean>();
        private final Map<String, GdxTextures.LoadedTexture> loaded =
                new HashMap<String, GdxTextures.LoadedTexture>();
        private final Set<String> loadedPaths = new HashSet<String>();
        private final Set<String> takeCalls = new HashSet<String>();
        private final AtomicInteger updateCalls = new AtomicInteger();
        private final AtomicInteger disposeCalls = new AtomicInteger();
        private GdxAssetManagerAssets.QueueErrorListener errorListener;

        @Override
        public void load(String path) {
            loadedPaths.add(path);
            pending.put(path, Boolean.TRUE);
        }

        @Override
        public boolean update() {
            updateCalls.incrementAndGet();
            if (pending.isEmpty()) {
                return true;
            }
            String path = pending.keySet().iterator().next();
            pending.remove(path);
            GdxTextures.LoadedTexture tex = catalog.get(path);
            if (tex == null) {
                if (errorListener != null) {
                    errorListener.error(path, new RuntimeException("missing " + path));
                }
                return pending.isEmpty();
            }
            loaded.put(path, tex);
            return pending.isEmpty();
        }

        @Override
        public float getProgress() {
            if (loaded.isEmpty() && pending.isEmpty()) {
                return 0f;
            }
            int total = loaded.size() + pending.size();
            return total == 0 ? 0f : ((float) loaded.size()) / total;
        }

        @Override
        public boolean isLoaded(String path) {
            return loaded.containsKey(path);
        }

        @Override
        public GdxTextures.LoadedTexture takeOwnership(String path) {
            takeCalls.add(path);
            return loaded.remove(path);
        }

        @Override
        public void setErrorListener(GdxAssetManagerAssets.QueueErrorListener listener) {
            this.errorListener = listener;
        }

        @Override
        public void cancel(String path) {
            pending.remove(path);
            loaded.remove(path);
        }

        @Override
        public void dispose() {
            disposeCalls.incrementAndGet();
            pending.clear();
            loaded.clear();
        }
    }
}
