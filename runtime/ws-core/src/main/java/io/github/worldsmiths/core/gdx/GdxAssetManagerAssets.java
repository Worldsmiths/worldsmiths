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
 * TextureLoader; {@link #pump()} calls {@link TextureAssetQueue#update()} once. Merges completed
 * textures into the shared {@link GdxTextures} cache. Not reachable from the web/TeaVM factory path
 * — see {@link GdxJvmEngineServices}. Not part of the author-facing API.
 */
final class GdxAssetManagerAssets implements GdxPumpableAssets {
    private final GdxTextures textures;
    private final TextureAssetQueue queue;
    private final Map<String, GdxLoadHandle> handlesByKey = new HashMap<String, GdxLoadHandle>();
    private final List<GdxLoadHandle> inFlight = new ArrayList<GdxLoadHandle>();
    private final Set<String> mergedAmPaths = new HashSet<String>();
    private final Map<String, String> pendingFailures = new HashMap<String, String>();
    private boolean disposed;

    GdxAssetManagerAssets(GdxTextures textures, TextureAssetQueue queue) {
        this.textures = textures;
        this.queue = queue;
        this.queue.setErrorListener(
                new QueueErrorListener() {
                    @Override
                    public void error(String path, Throwable throwable) {
                        String message =
                                "Missing or unloadable texture: "
                                        + path
                                        + (throwable != null && throwable.getMessage() != null
                                                ? " (" + throwable.getMessage() + ")"
                                                : "");
                        pendingFailures.put(path, message);
                    }
                });
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
        if (kind == AssetKind.MESH || kind == AssetKind.MUSIC) {
            failImmediate(handle, "Unsupported asset kind: " + kind);
            return handle;
        }
        if (kind == AssetKind.TEXTURE && textures.getIfPresent(path) != null) {
            handle.state = LoadState.COMPLETE;
            handle.progress = 1f;
            handle.fireTerminal();
            return handle;
        }
        handle.state = LoadState.QUEUED;
        handle.progress = 0f;
        textures.beginAsyncLoad(path);
        inFlight.add(handle);
        queue.load(path);
        return handle;
    }

    /**
     * One AssetManager {@code update()} step, then merges completed TEXTURE handles into {@link
     * GdxTextures} and applies coarse queue progress. Called once per frame before {@code
     * game.update}.
     */
    @Override
    public void pump() {
        if (disposed) {
            return;
        }
        queue.update();
        float coarse = queue.getProgress();
        List<GdxLoadHandle> snapshot = new ArrayList<GdxLoadHandle>(inFlight);
        for (GdxLoadHandle handle : snapshot) {
            String failure = pendingFailures.remove(handle.path);
            if (failure != null) {
                failHandle(handle, failure);
                continue;
            }
            if (queue.isLoaded(handle.path)) {
                GdxTextures.LoadedTexture loaded = queue.takeOwnership(handle.path);
                if (loaded == null) {
                    failHandle(handle, "Missing or unloadable texture: " + handle.path);
                    continue;
                }
                textures.putFromAsyncManagedExternally(handle.path, loaded);
                mergedAmPaths.add(handle.path);
                handle.state = LoadState.COMPLETE;
                handle.progress = 1f;
                inFlight.remove(handle);
                handle.fireTerminal();
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
            queue.cancel(handle.path);
            textures.endAsyncLoad(handle.path);
            handle.state = LoadState.FAILED;
            handle.progress = 0f;
            handle.failureMessage = "Disposed while loading: " + handle.path;
            handle.fireTerminal();
        }
        inFlight.clear();
        for (String path : mergedAmPaths) {
            textures.evictWithoutDispose(path);
        }
        mergedAmPaths.clear();
        pendingFailures.clear();
        queue.dispose();
    }

    private void failImmediate(GdxLoadHandle handle, String message) {
        handle.state = LoadState.FAILED;
        handle.progress = 0f;
        handle.failureMessage = message;
        handle.fireTerminal();
    }

    private void failHandle(GdxLoadHandle handle, String message) {
        queue.cancel(handle.path);
        textures.endAsyncLoad(handle.path);
        handle.state = LoadState.FAILED;
        handle.progress = 0f;
        handle.failureMessage = message;
        inFlight.remove(handle);
        handle.fireTerminal();
    }

    private static String handleKey(AssetKind kind, String path) {
        return kind + "\0" + path;
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

    interface QueueErrorListener {
        void error(String path, Throwable throwable);
    }
}
