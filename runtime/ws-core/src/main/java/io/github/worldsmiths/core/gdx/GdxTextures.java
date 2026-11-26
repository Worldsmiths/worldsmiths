package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Textures;
import java.util.HashMap;
import java.util.Map;

/**
 * GDX-backed {@link Textures} with a sync FileHandle-managed texture cache keyed by internal path.
 * Not part of the author-facing API — backends assemble this via {@link GdxEngineServices}.
 */
final class GdxTextures implements Textures {
    private final Map<String, LoadedTexture> cache = new HashMap<String, LoadedTexture>();
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
        return ensureCached(internalPath).gdxTexture();
    }

    private LoadedTexture ensureCached(String internalPath) {
        LoadedTexture existing = cache.get(internalPath);
        if (existing != null) {
            return existing;
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
