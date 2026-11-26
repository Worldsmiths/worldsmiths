package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import io.github.worldsmiths.core.Assets;
import io.github.worldsmiths.core.EngineServices;

/**
 * JVM-only factory for a GDX-backed {@link EngineServices} bag with LibGDX {@link AssetManager}
 * async textures. Used by desktop / Android / iOS listeners. Intentionally separate from {@link
 * GdxEngineServices} so the web/TeaVM call graph never reaches {@code AssetManager} / {@code
 * AsyncExecutor}.
 */
public final class GdxJvmEngineServices {
    private GdxJvmEngineServices() {}

    /**
     * Assembles the same bag shape as {@link GdxEngineServices#createCooperative()} but wires
     * AssetManager-backed assets for off-GL texture prepare on JVM backends.
     */
    public static EngineServices create() {
        GdxTextures textures = new GdxTextures();
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(textures, libGdxTextureAssetQueue());
        return GdxEngineServices.bag(textures, assets);
    }

    /**
     * Package-visible headless seam: JVM AssetManager-backed assets with an injectable queue (no
     * live Application / GL).
     */
    static Assets newAssetsForTest(
            GdxTextures textures, GdxAssetManagerAssets.TextureAssetQueue queue) {
        return new GdxAssetManagerAssets(textures, queue);
    }

    static GdxAssetManagerAssets.TextureAssetQueue libGdxTextureAssetQueue() {
        final AssetManager assetManager = new AssetManager();
        return new GdxAssetManagerAssets.TextureAssetQueue() {
            private GdxAssetManagerAssets.QueueErrorListener listener;

            {
                assetManager.setErrorListener(
                        new AssetErrorListener() {
                            @Override
                            public void error(AssetDescriptor asset, Throwable throwable) {
                                if (listener != null && asset != null) {
                                    listener.error(asset.fileName, throwable);
                                }
                            }
                        });
            }

            @Override
            public void load(String path) {
                assetManager.load(path, Texture.class);
            }

            @Override
            public boolean update() {
                return assetManager.update();
            }

            @Override
            public float getProgress() {
                return assetManager.getProgress();
            }

            @Override
            public boolean isLoaded(String path) {
                return assetManager.isLoaded(path, Texture.class);
            }

            @Override
            public GdxTextures.LoadedTexture takeOwnership(String path) {
                final Texture texture = assetManager.get(path, Texture.class);
                return new GdxTextures.LoadedTexture() {
                    @Override
                    public Texture gdxTexture() {
                        return texture;
                    }

                    @Override
                    public void dispose() {
                        // AssetManager owns dispose until queue.dispose() / assetManager.dispose().
                    }
                };
            }

            @Override
            public void setErrorListener(GdxAssetManagerAssets.QueueErrorListener errorListener) {
                this.listener = errorListener;
            }

            @Override
            public void cancel(String path) {
                if (assetManager.contains(path)) {
                    assetManager.unload(path);
                }
            }

            @Override
            public void dispose() {
                assetManager.dispose();
            }
        };
    }
}
