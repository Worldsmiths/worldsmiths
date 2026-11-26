package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Textures;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GDX-backed {@link Textures} with a sync FileHandle-managed texture cache keyed by internal path.
 * Async {@link GdxAssets} merges completes into the same cache and marks in-flight keys so retained
 * draw can skip without a sync open. Sync {@link #preload}/{@link #resolve} refuse paths still in
 * flight (not yet cached). Not part of the author-facing API — backends assemble this via {@link
 * GdxEngineServices#createCooperative()} or {@link GdxJvmEngineServices#create()}.
 */
final class GdxTextures implements Textures {
    private final Map<String, LoadedTexture> cache = new HashMap<String, LoadedTexture>();
    private final Set<String> asyncInFlight = new HashSet<String>();
    /** Paths whose GPU lifetime is owned by AssetManager / external queue — skip in {@link #dispose()}. */
    private final Set<String> externallyOwned = new HashSet<String>();
    private final TextureOpener opener;

    GdxTextures() {
        this(
                new TextureOpener() {
                    @Override
                    public LoadedTexture open(String path) {
                        FileHandle handle = Gdx.files.internal(path);
                        if (handle == null || !handle.exists()) {
                            return null;
                        }
                        final Texture texture = new Texture(handle);
                        return new LoadedTexture() {
                            @Override
                            public Texture gdxTexture() {
                                return texture;
                            }

                            @Override
                            public void dispose() {
                                texture.dispose();
                            }
                        };
                    }
                });
    }

    /** Package-visible for tests that inject textures without a live Application / GL context. */
    GdxTextures(TextureOpener opener) {
        this.opener = opener;
    }

    @Override
    public void preload(String internalPath) {
        ensureCached(internalPath);
    }

    @Override
    public void resolve(String internalPath) {
        ensureCached(internalPath);
    }

    /**
     * Returns the cached LibGDX texture for {@code internalPath}, loading on miss. Used by scene
     * renderers in this package.
     *
     * @throws EngineException if the path is missing or cannot be loaded
     */
    Texture require(String internalPath) {
        return requireLoaded(internalPath).gdxTexture();
    }

    /**
     * Returns the cached {@link LoadedTexture} for {@code internalPath}, loading on miss. Package-
     * visible so tests can assert cache identity without a live GL {@link Texture}.
     *
     * @throws EngineException if the path is missing or cannot be loaded
     */
    LoadedTexture requireLoaded(String internalPath) {
        return ensureCached(internalPath);
    }

    /** Cache hit without sync open; {@code null} when absent. */
    LoadedTexture getIfPresent(String internalPath) {
        return cache.get(internalPath);
    }

    /** True while an async TEXTURE request for {@code path} is in flight (not yet cached). */
    boolean isAsyncInFlight(String internalPath) {
        return asyncInFlight.contains(internalPath);
    }

    void beginAsyncLoad(String internalPath) {
        asyncInFlight.add(internalPath);
    }

    void endAsyncLoad(String internalPath) {
        asyncInFlight.remove(internalPath);
    }

    /**
     * Opens via the injected opener without putting into the cache — used by {@link GdxAssets}
     * during pump. Returns {@code null} on miss; may throw on unloadable decode.
     */
    LoadedTexture openForAsync(String internalPath) {
        return opener.open(internalPath);
    }

    /**
     * Merges an async-loaded texture into the sync cache (no second open). Clears the in-flight
     * mark for {@code internalPath}. Engine owns dispose of {@code loaded}.
     */
    void putFromAsync(String internalPath, LoadedTexture loaded) {
        if (loaded == null) {
            return;
        }
        if (!cache.containsKey(internalPath)) {
            cache.put(internalPath, loaded);
        }
        asyncInFlight.remove(internalPath);
    }

    /**
     * Merges an AssetManager-owned texture into the sync cache without taking GPU dispose
     * ownership. {@link #dispose()} will not call {@link LoadedTexture#dispose()} for this key —
     * callers must {@link #evictWithoutDispose(String)} before the AssetManager tears down.
     */
    void putFromAsyncManagedExternally(String internalPath, LoadedTexture loaded) {
        if (loaded == null) {
            return;
        }
        if (!cache.containsKey(internalPath)) {
            cache.put(internalPath, loaded);
            externallyOwned.add(internalPath);
        }
        asyncInFlight.remove(internalPath);
    }

    /** Removes a cache entry without disposing the underlying GPU resource. */
    void evictWithoutDispose(String internalPath) {
        cache.remove(internalPath);
        externallyOwned.remove(internalPath);
        asyncInFlight.remove(internalPath);
    }

    /**
     * Disposes every engine-owned cached texture and clears the cache. Externally (AssetManager)
     * owned entries are removed without a second dispose. Idempotent — safe at listener / bag
     * teardown. Engine-owned; authors do not call this.
     */
    void dispose() {
        for (Map.Entry<String, LoadedTexture> entry : cache.entrySet()) {
            if (!externallyOwned.contains(entry.getKey())) {
                entry.getValue().dispose();
            }
        }
        cache.clear();
        externallyOwned.clear();
        asyncInFlight.clear();
    }

    /** Package-visible for headless dispose assertions. */
    int cacheSizeForTest() {
        return cache.size();
    }

    private LoadedTexture ensureCached(String internalPath) {
        LoadedTexture existing = cache.get(internalPath);
        if (existing != null) {
            return existing;
        }
        if (asyncInFlight.contains(internalPath)) {
            throw new EngineException(
                    "Async load in flight for path; cannot sync-open: " + internalPath);
        }
        LoadedTexture loaded;
        try {
            loaded = opener.open(internalPath);
        } catch (RuntimeException e) {
            throw new EngineException("Failed to load texture: " + internalPath, e);
        }
        if (loaded == null) {
            throw new EngineException("Missing or unloadable texture: " + internalPath);
        }
        cache.put(internalPath, loaded);
        return loaded;
    }

    /**
     * Resolves an internal path to a loaded managed texture. Return {@code null} when the path is
     * missing; throw on unloadable decode failures.
     */
    interface TextureOpener {
        LoadedTexture open(String path);
    }

    /**
     * Thin seam over {@link Texture} so unit tests can inject recording stubs without constructing
     * a real GL texture.
     */
    interface LoadedTexture {
        Texture gdxTexture();

        void dispose();
    }
}
