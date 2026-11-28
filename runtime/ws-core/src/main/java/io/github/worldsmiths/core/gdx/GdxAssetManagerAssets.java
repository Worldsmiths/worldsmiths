package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JVM AssetManager-backed {@link io.github.worldsmiths.core.Assets}. Off-thread prepare via LibGDX
 * TextureLoader and gdx-gltf SceneAsset loaders; {@link #pump()} calls queue {@code update()} once.
 * Merges completed textures into {@link GdxTextures} and meshes into {@link GdxMeshes}. Not
 * reachable from the web/TeaVM factory path — see {@link GdxJvmEngineServices}. Not part of the
 * author-facing API.
 */
final class GdxAssetManagerAssets implements GdxPumpableAssets {
    private final GdxTextures textures;
    private final GdxMeshes meshes;
    private final TextureAssetQueue textureQueue;
    private final MeshAssetQueue meshQueue;
    private final Map<String, GdxLoadHandle> handlesByKey = new HashMap<String, GdxLoadHandle>();
    private final List<GdxLoadHandle> inFlight = new ArrayList<GdxLoadHandle>();
    private final Set<String> mergedAmTexturePaths = new HashSet<String>();
    private final Set<String> mergedAmMeshPaths = new HashSet<String>();
    private final Map<String, String> pendingFailures = new HashMap<String, String>();
    private boolean disposed;

    GdxAssetManagerAssets(
            GdxTextures textures,
            GdxMeshes meshes,
            TextureAssetQueue textureQueue,
            MeshAssetQueue meshQueue) {
        this.textures = textures;
        this.meshes = meshes;
        this.textureQueue = textureQueue;
        this.meshQueue = meshQueue;
        QueueErrorListener listener =
                new QueueErrorListener() {
                    @Override
                    public void error(String path, Throwable throwable) {
                        String message =
                                pendingFailureMessage(path, throwable);
                        pendingFailures.put(path, message);
                    }
                };
        this.textureQueue.setErrorListener(listener);
        this.meshQueue.setErrorListener(listener);
    }

    @Override
    public GdxMeshes meshesForD3Bind() {
        return meshes;
    }

    @Override
    public LoadHandle request(String path, AssetKind kind) {
        String key = handleKey(kind, path);
        GdxLoadHandle existing = handlesByKey.get(key);
        if (existing != null) {
            if (existing.state == LoadState.FAILED) {
                handlesByKey.remove(key);
            } else {
                return existing;
            }
        }
        GdxLoadHandle handle = new GdxLoadHandle(path, kind);
        handlesByKey.put(key, handle);
        if (disposed) {
            failImmediate(handle, "Assets disposed; cannot request: " + path);
            return handle;
        }
        if (path == null || path.trim().isEmpty()) {
            failImmediate(handle, "Asset path is null or blank");
            return handle;
        }
        if (kind == AssetKind.MUSIC) {
            failImmediate(handle, "Unsupported asset kind: " + kind);
            return handle;
        }
        if (kind == AssetKind.TEXTURE && textures.getIfPresent(path) != null) {
            handle.state = LoadState.COMPLETE;
            handle.progress = 1f;
            handle.fireTerminal();
            return handle;
        }
        if (kind == AssetKind.MESH && meshes.getIfPresent(path) != null) {
            handle.state = LoadState.COMPLETE;
            handle.progress = 1f;
            handle.fireTerminal();
            return handle;
        }
        handle.state = LoadState.QUEUED;
        handle.progress = 0f;
        inFlight.add(handle);
        if (kind == AssetKind.TEXTURE) {
            textures.beginAsyncLoad(path);
            textureQueue.load(path);
        } else if (kind == AssetKind.MESH) {
            meshes.beginAsyncLoad(path);
            meshQueue.loadMesh(path);
        }
        return handle;
    }

    /**
     * One queue {@code update()} step, then merges completed TEXTURE/MESH handles and applies coarse
     * queue progress. Called once per frame before {@code game.update}.
     */
    @Override
    public void pump() {
        if (disposed) {
            return;
        }
        textureQueue.update();
        float coarse = textureQueue.getProgress();
        List<GdxLoadHandle> snapshot = new ArrayList<GdxLoadHandle>(inFlight);
        for (GdxLoadHandle handle : snapshot) {
            String failure = pendingFailures.remove(handle.path);
            if (failure != null) {
                failHandle(handle, failure);
                continue;
            }
            if (handle.kind == AssetKind.TEXTURE && textureQueue.isLoaded(handle.path)) {
                completeTexture(handle);
                continue;
            }
            if (handle.kind == AssetKind.MESH && meshQueue.isMeshLoaded(handle.path)) {
                completeMesh(handle);
                continue;
            }
            if (handle.state == LoadState.QUEUED || handle.state == LoadState.LOADING) {
                handle.state = LoadState.LOADING;
                handle.progress = Math.max(0f, Math.min(0.99f, coarse));
            }
        }
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        List<GdxLoadHandle> snapshot = new ArrayList<GdxLoadHandle>(inFlight);
        for (GdxLoadHandle handle : snapshot) {
            cancelInFlight(handle);
            if (handle.kind == AssetKind.TEXTURE) {
                textures.endAsyncLoad(handle.path);
            } else if (handle.kind == AssetKind.MESH) {
                meshes.endAsyncLoad(handle.path);
            }
            handle.state = LoadState.FAILED;
            handle.progress = 0f;
            handle.failureMessage = "Disposed while loading: " + handle.path;
            handle.fireTerminal();
        }
        inFlight.clear();
        for (String path : mergedAmTexturePaths) {
            textures.evictWithoutDispose(path);
        }
        mergedAmTexturePaths.clear();
        for (String path : mergedAmMeshPaths) {
            meshes.evictWithoutDispose(path);
        }
        mergedAmMeshPaths.clear();
        pendingFailures.clear();
        textureQueue.dispose();
        if (meshQueue != textureQueue) {
            meshQueue.dispose();
        }
    }

    private void completeTexture(GdxLoadHandle handle) {
        GdxTextures.LoadedTexture loaded = textureQueue.takeOwnership(handle.path);
        if (loaded == null) {
            failHandle(handle, missingAssetMessage("texture", handle.path));
            return;
        }
        textures.putFromAsyncManagedExternally(handle.path, loaded);
        mergedAmTexturePaths.add(handle.path);
        terminalComplete(handle);
    }

    private void completeMesh(GdxLoadHandle handle) {
        GdxMeshes.LoadedMesh loaded = meshQueue.takeMeshOwnership(handle.path);
        if (loaded == null) {
            failHandle(handle, missingAssetMessage("mesh", handle.path));
            return;
        }
        meshes.putFromAsyncManagedExternally(handle.path, loaded);
        mergedAmMeshPaths.add(handle.path);
        terminalComplete(handle);
    }

    private void terminalComplete(GdxLoadHandle handle) {
        handle.state = LoadState.COMPLETE;
        handle.progress = 1f;
        inFlight.remove(handle);
        handle.fireTerminal();
    }

    private void cancelInFlight(GdxLoadHandle handle) {
        if (handle.kind == AssetKind.TEXTURE) {
            textureQueue.cancel(handle.path);
        } else if (handle.kind == AssetKind.MESH) {
            meshQueue.cancel(handle.path);
        }
    }

    private void failImmediate(GdxLoadHandle handle, String message) {
        handle.state = LoadState.FAILED;
        handle.progress = 0f;
        handle.failureMessage = message;
        handle.fireTerminal();
    }

    private void failHandle(GdxLoadHandle handle, String message) {
        cancelInFlight(handle);
        if (handle.kind == AssetKind.TEXTURE) {
            textures.endAsyncLoad(handle.path);
        } else if (handle.kind == AssetKind.MESH) {
            meshes.endAsyncLoad(handle.path);
        }
        handle.state = LoadState.FAILED;
        handle.progress = 0f;
        handle.failureMessage = message;
        inFlight.remove(handle);
        handle.fireTerminal();
    }

    private static String handleKey(AssetKind kind, String path) {
        return kind + "\0" + path;
    }

    private static String missingAssetMessage(String kind, String path) {
        return "Missing or unloadable " + kind + ": " + path;
    }

    private static String pendingFailureMessage(String path, Throwable throwable) {
        return missingAssetMessage("asset", path)
                + (throwable != null && throwable.getMessage() != null
                        ? " (" + throwable.getMessage() + ")"
                        : "");
    }

    /**
     * Injectable LibGDX {@code AssetManager} seam for TEXTURE loads. Production wraps a real
     * AssetManager; unit tests inject a recording queue without GL.
     */
    interface TextureAssetQueue {
        void load(String path);

        /** One {@code AssetManager.update()} step. */
        boolean update();

        float getProgress();

        boolean isLoaded(String path);

        /**
         * Returns the loaded texture for merge into {@link GdxTextures}. Caller must treat dispose
         * ownership as remaining with this queue until {@link #dispose()} (no double-dispose).
         */
        GdxTextures.LoadedTexture takeOwnership(String path);

        void setErrorListener(QueueErrorListener listener);

        void cancel(String path);

        void dispose();
    }

    /** Injectable AssetManager seam for glTF MESH loads via gdx-gltf {@code SceneAsset} loaders. */
    interface MeshAssetQueue {
        void loadMesh(String path);

        boolean update();

        float getProgress();

        boolean isMeshLoaded(String path);

        GdxMeshes.LoadedMesh takeMeshOwnership(String path);

        void setErrorListener(QueueErrorListener listener);

        void cancel(String path);

        void dispose();
    }

    interface QueueErrorListener {
        void error(String path, Throwable throwable);
    }
}