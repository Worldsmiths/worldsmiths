package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cooperative GDX-backed assets for TeaVM/web (and headless tests that inject openers). TEXTURE
 * and MESH loads run synchronously on {@link #pump()} (≤1 heavy open per pump), then merge into
 * {@link GdxTextures} / {@link GdxMeshes}. No {@code Thread} or threaded loader stack — safe for
 * the web-reachable call graph. Not part of the author-facing API — backends assemble this via
 * {@link GdxEngineServices#createCooperative()}.
 */
final class GdxAssets implements GdxPumpableAssets {
    private final GdxTextures textures;
    private final GdxMeshes meshes;
    private final Map<String, GdxLoadHandle> handlesByKey = new HashMap<String, GdxLoadHandle>();
    private final List<GdxLoadHandle> inFlight = new ArrayList<GdxLoadHandle>();
    private boolean disposed;

    GdxAssets(GdxTextures textures) {
        this(textures, new GdxMeshes());
    }

    GdxAssets(GdxTextures textures, GdxMeshes meshes) {
        this.textures = textures;
        this.meshes = meshes;
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
        if (kind == AssetKind.TEXTURE) {
            textures.beginAsyncLoad(path);
        } else if (kind == AssetKind.MESH) {
            meshes.beginAsyncLoad(path);
        }
        inFlight.add(handle);
        return handle;
    }

    /**
     * Advances at most one in-flight TEXTURE or MESH open this frame, merges on success, and fires
     * terminal listeners. Called once per frame before {@code game.update}.
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
        if (next.kind == AssetKind.TEXTURE) {
            pumpTexture(next);
        } else if (next.kind == AssetKind.MESH) {
            pumpMesh(next);
        }
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
        meshes.dispose();
    }

    private void pumpTexture(GdxLoadHandle next) {
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
        terminalComplete(next);
    }

    private void pumpMesh(GdxLoadHandle next) {
        GdxMeshes.LoadedMesh loaded;
        try {
            loaded = meshes.openForAsync(next.path);
        } catch (RuntimeException e) {
            failHandle(next, "Missing or unloadable mesh: " + next.path);
            return;
        }
        if (loaded == null) {
            failHandle(next, "Missing or unloadable mesh: " + next.path);
            return;
        }
        meshes.putFromAsync(next.path, loaded);
        meshes.endAsyncLoad(next.path);
        terminalComplete(next);
    }

    private void terminalComplete(GdxLoadHandle handle) {
        handle.state = LoadState.COMPLETE;
        handle.progress = 1f;
        inFlight.remove(handle);
        handle.fireTerminal();
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
}