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
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Headless GdxAssets coverage via injectable texture opener — no Application / display / GL
 * required.
 */
class GdxAssetsTest {

    @Test
    void missingTextureRequestDoesNotThrowAndReachesFailedWithPathAfterPump() {
        AtomicInteger opens = new AtomicInteger();
        GdxTextures textures = texturesWithCatalog(opens, null);
        GdxAssets assets = new GdxAssets(textures);

        LoadHandle handle = assets.request("textures/missing.png", AssetKind.TEXTURE);

        assertNotNull(handle);
        assertEquals(LoadState.QUEUED, handle.state());

        assets.pump();

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().contains("textures/missing.png"));
        assertEquals(1, opens.get());
    }

    @Test
    void completeMergesIntoSameCacheSoResolveDoesNotReopen() {
        AtomicInteger opens = new AtomicInteger();
        final GdxTextures.LoadedTexture stub = stubLoaded();
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        catalog.put("textures/heavy.png", stub);
        GdxTextures textures = texturesWithCatalog(opens, catalog);
        GdxAssets assets = new GdxAssets(textures);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertTrue(textures.isAsyncInFlight("textures/heavy.png"));
        assets.pump();

        assertEquals(LoadState.COMPLETE, handle.state());
        assertEquals(1f, handle.progress(), 1e-5f);
        assertFalse(textures.isAsyncInFlight("textures/heavy.png"));
        assertEquals(1, opens.get());

        textures.resolve("textures/heavy.png");
        assertSame(stub, textures.requireLoaded("textures/heavy.png"));
        assertEquals(1, opens.get());
    }

    @Test
    void meshRequestCompletesAfterPumpAndMergesIntoMeshRegistry() {
        AtomicInteger meshOpens = new AtomicInteger();
        final GdxMeshes.LoadedMesh stub = stubLoadedMesh();
        Map<String, GdxMeshes.LoadedMesh> meshCatalog = new HashMap<String, GdxMeshes.LoadedMesh>();
        meshCatalog.put("models/hero.gltf", stub);
        GdxMeshes meshes = meshesWithCatalog(meshOpens, meshCatalog);
        GdxAssets assets = new GdxAssets(texturesWithCatalog(new AtomicInteger(), null), meshes);

        LoadHandle mesh = assets.request("models/hero.gltf", AssetKind.MESH);
        assertEquals(LoadState.QUEUED, mesh.state());
        assertTrue(meshes.isAsyncInFlight("models/hero.gltf"));

        assets.pump();

        assertEquals(LoadState.COMPLETE, mesh.state());
        assertSame(stub, meshes.getIfPresent("models/hero.gltf"));
        assertEquals(1, meshOpens.get());
    }

    @Test
    void meshMissReachesFailedAfterPumpWithoutThrowOnRequest() {
        AtomicInteger meshOpens = new AtomicInteger();
        GdxMeshes meshes = meshesWithCatalog(meshOpens, null);
        GdxAssets assets = new GdxAssets(texturesWithCatalog(new AtomicInteger(), null), meshes);

        LoadHandle mesh = assets.request("models/missing.gltf", AssetKind.MESH);
        assertNotNull(mesh);
        assertEquals(LoadState.QUEUED, mesh.state());

        assets.pump();

        assertEquals(LoadState.FAILED, mesh.state());
        assertNotNull(mesh.failureMessage());
        assertTrue(mesh.failureMessage().contains("models/missing.gltf"));
        assertNull(meshes.getIfPresent("models/missing.gltf"));
        assertEquals(1, meshOpens.get());
    }

    @Test
    void musicRequestStillUnsupportedOnCooperativePath() {
        GdxAssets assets = new GdxAssets(texturesWithCatalog(new AtomicInteger(), null));

        LoadHandle music = assets.request("audio/theme.ogg", AssetKind.MUSIC);
        assertEquals(LoadState.FAILED, music.state());
        assertTrue(music.failureMessage().contains("MUSIC"));
    }

    @Test
    void reRequestSharesInFlightAndCompletedHandle() {
        AtomicInteger opens = new AtomicInteger();
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        catalog.put("textures/heavy.png", stubLoaded());
        GdxTextures textures = texturesWithCatalog(opens, catalog);
        GdxAssets assets = new GdxAssets(textures);

        LoadHandle first = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        LoadHandle second = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertSame(first, second);

        assets.pump();
        assertEquals(LoadState.COMPLETE, first.state());
        LoadHandle third = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertSame(first, third);
        assertEquals(1, opens.get());
    }

    @Test
    void reRequestAfterFailedStartsFreshLoadThatCanCompleteIndependently() {
        AtomicInteger opens = new AtomicInteger();
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        GdxTextures textures = texturesWithCatalog(opens, catalog);
        GdxAssets assets = new GdxAssets(textures);

        LoadHandle first = assets.request("textures/retry.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.FAILED, first.state());

        catalog.put("textures/retry.png", stubLoaded());
        LoadHandle second = assets.request("textures/retry.png", AssetKind.TEXTURE);

        assertNotSame(first, second);
        assertEquals(LoadState.QUEUED, second.state());
        assertEquals(LoadState.FAILED, first.state());

        assets.pump();
        assertEquals(LoadState.COMPLETE, second.state());
        assertEquals(LoadState.FAILED, first.state());
        assertEquals(2, opens.get());
    }

    @Test
    void onTerminalFiresOnPumpCompleteWithoutEngineLog() {
        AtomicInteger opens = new AtomicInteger();
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        catalog.put("textures/heavy.png", stubLoaded());
        GdxTextures textures = texturesWithCatalog(opens, catalog);
        GdxAssets assets = new GdxAssets(textures);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        final AtomicBoolean fired = new AtomicBoolean();
        handle.onTerminal(
                new Runnable() {
                    @Override
                    public void run() {
                        fired.set(true);
                    }
                });

        assertFalse(fired.get());
        assets.pump();
        assertTrue(fired.get());
        assertEquals(LoadState.COMPLETE, handle.state());
    }

    @Test
    void disposeFailsInFlightSafelyAndClearsInFlightMark() {
        AtomicInteger opens = new AtomicInteger();
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        catalog.put("textures/heavy.png", stubLoaded());
        GdxTextures textures = texturesWithCatalog(opens, catalog);
        GdxAssets assets = new GdxAssets(textures);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertTrue(textures.isAsyncInFlight("textures/heavy.png"));

        assets.dispose();

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertFalse(textures.isAsyncInFlight("textures/heavy.png"));
        assertEquals(0, opens.get());

        assets.pump(); // no-op after dispose
        assertNull(textures.getIfPresent("textures/heavy.png"));
    }

    @Test
    void requestAfterDisposeReturnsFailedWithoutTouchingOpener() {
        AtomicInteger opens = new AtomicInteger();
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        catalog.put("textures/late.png", stubLoaded());
        GdxTextures textures = texturesWithCatalog(opens, catalog);
        GdxAssets assets = new GdxAssets(textures);

        assets.dispose();

        LoadHandle handle = assets.request("textures/late.png", AssetKind.TEXTURE);

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().toLowerCase().contains("disposed"));
        assertFalse(textures.isAsyncInFlight("textures/late.png"));
        assertEquals(0, opens.get());
        assertNull(textures.getIfPresent("textures/late.png"));
    }

    @Test
    void bagAssetsReturnsNonNullAndPumpAssetsAdvancesLoad() {
        AtomicInteger opens = new AtomicInteger();
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        catalog.put("textures/heavy.png", stubLoaded());
        GdxTextures textures = texturesWithCatalog(opens, catalog);
        GdxAssets assets = new GdxAssets(textures);
        EngineServices bag = GdxEngineServices.bagForAssetsTest(textures, assets);

        assertNotNull(bag.assets());
        assertSame(assets, bag.assets());
        assertSame(textures, bag.textures());

        LoadHandle handle = bag.assets().request("textures/heavy.png", AssetKind.TEXTURE);
        GdxEngineServices.pumpAssets(bag);
        assertEquals(LoadState.COMPLETE, handle.state());

        GdxEngineServices.disposeTextures(bag);
        assertEquals(0, textures.cacheSizeForTest());
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

    private static GdxTextures texturesWithCatalog(
            final AtomicInteger opens, final Map<String, GdxTextures.LoadedTexture> catalog) {
        return new GdxTextures(
                new GdxTextures.TextureOpener() {
                    @Override
                    public GdxTextures.LoadedTexture open(String path) {
                        opens.incrementAndGet();
                        if (catalog == null) {
                            return null;
                        }
                        return catalog.get(path);
                    }
                });
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

    private static GdxMeshes meshesWithCatalog(
            final AtomicInteger opens, final Map<String, GdxMeshes.LoadedMesh> catalog) {
        return new GdxMeshes(
                new GdxMeshes.MeshOpener() {
                    @Override
                    public GdxMeshes.LoadedMesh open(String path) {
                        opens.incrementAndGet();
                        if (catalog == null) {
                            return null;
                        }
                        return catalog.get(path);
                    }
                });
    }
}

