package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cooperative GDX-backed assets for TeaVM/web (and headless tests that inject a {@link
 * GdxTextures.TextureOpener}). TEXTURE loads open via the shared {@link GdxTextures} opener on
 * {@link #pump()} (≤1 open per pump), then merge into the same texture cache. No {@code Thread} or
 * threaded loader stack — safe for the web-reachable call graph. Not part of the author-facing API
 * — backends assemble this via {@link GdxEngineServices#createCooperative()}.
 */
final class GdxAssets implements GdxPumpableAssets {
    private final GdxTextures textures;
    private final Map<String, GdxLoadHandle> handlesByKey = new HashMap<String, GdxLoadHandle>();
    private final List<GdxLoadHandle> inFlight = new ArrayList<GdxLoadHandle>();
    private boolean disposed;

    GdxAssets(GdxTextures textures) {
        this.textures = textures;
    }

    @Override
    public LoadHandle request(String path, AssetKind kind) {
        String key = handleKey(kind, path);
        GdxLoadHandle existing = handlesByKey.get(key);
        if (existing != null) {
            return existing;
        }
        GdxLoadHandle handle = new GdxLoadHandle(path, kind);
        handlesByKey.put(key, handle);
        if (path == null || path.trim().isEmpty()) {
            failImmediate(handle, "Asset path is null or blank");
            return handle;
        }
        if (kind == AssetKind.MESH || kind == AssetKind.MUSIC) {
            failImmediate(handle, "Unsupported asset kind: " + kind);
            return handle;
        }
        // Already in the sync texture cache → COMPLETE without enqueue.
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
        return handle;
    }

    /**
     * Advances at most one in-flight TEXTURE open this frame, merges into {@link GdxTextures} on
     * success, and fires terminal listeners. Called once per frame before {@code game.update}.
     */
    @Override
    public void pump() {
        if (disposed) {
            return;
        }
        GdxLoadHandle next = nextQueuedOrLoading();
        if (next == null) {
            return;
        }
        next.state = LoadState.LOADING;
        next.progress = 0.5f;
        GdxTextures.LoadedTexture loaded;
        try {
            loaded = textures.openForAsync(next.path);
        } catch (RuntimeException e) {
            failHandle(next, "Missing or unloadable texture: " + next.path);
            return;
        }
        if (loaded == null) {
            failHandle(next, "Missing or unloadable texture: " + next.path);
            return;
        }
        textures.putFromAsync(next.path, loaded);
        textures.endAsyncLoad(next.path);
        next.state = LoadState.COMPLETE;
        next.progress = 1f;
        inFlight.remove(next);
        next.fireTerminal();
    }

    /** Tears down in-flight loads safely. Idempotent. */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        List<GdxLoadHandle> snapshot = new ArrayList<GdxLoadHandle>(inFlight);
        for (GdxLoadHandle handle : snapshot) {
            textures.endAsyncLoad(handle.path);
            handle.state = LoadState.FAILED;
            handle.progress = 0f;
            handle.failureMessage = "Disposed while loading: " + handle.path;
            handle.fireTerminal();
        }
        inFlight.clear();
    }

    private GdxLoadHandle nextQueuedOrLoading() {
        for (GdxLoadHandle handle : inFlight) {
            if (handle.state == LoadState.QUEUED || handle.state == LoadState.LOADING) {
                return handle;
            }
        }
        return null;
    }

    private void failImmediate(GdxLoadHandle handle, String message) {
        handle.state = LoadState.FAILED;
        handle.progress = 0f;
        handle.failureMessage = message;
        handle.fireTerminal();
    }

    private void failHandle(GdxLoadHandle handle, String message) {
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
}
