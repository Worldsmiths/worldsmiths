package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import io.github.worldsmiths.core.Assets;
import io.github.worldsmiths.core.EngineServices;
import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * JVM-only factory for a GDX-backed {@link EngineServices} bag with LibGDX {@link AssetManager}
 * async textures and gdx-gltf MESH loads. Used by desktop / Android / iOS listeners.
 * Intentionally separate from {@link GdxEngineServices} so the web/TeaVM call graph never reaches
 * {@code AssetManager} / {@code AsyncExecutor}.
 */
public final class GdxJvmEngineServices {
    private GdxJvmEngineServices() {}

    /**
     * Assembles the same bag shape as {@link GdxEngineServices#createCooperative()} but wires
     * AssetManager-backed assets for off-GL texture and glTF prepare on JVM backends.
     */
    public static EngineServices create() {
        GdxTextures textures = new GdxTextures();
        GdxMeshes meshes = new GdxMeshes();
        LibGdxAssetQueues queues = libGdxAssetQueues();
        GdxAssetManagerAssets assets =
                new GdxAssetManagerAssets(textures, meshes, queues, queues);
        return GdxEngineServices.bag(textures, assets);
    }

    /**
     * Package-visible headless seam: JVM AssetManager-backed assets with injectable queues (no live
     * Application / GL).
     */
    static Assets newAssetsForTest(
            GdxTextures textures,
            GdxMeshes meshes,
            GdxAssetManagerAssets.TextureAssetQueue textureQueue,
            GdxAssetManagerAssets.MeshAssetQueue meshQueue) {
        return new GdxAssetManagerAssets(textures, meshes, textureQueue, meshQueue);
    }

    /** Back-compat overload for hybrid-factory tests that only exercise TEXTURE queues. */
    static Assets newAssetsForTest(
            GdxTextures textures, GdxAssetManagerAssets.TextureAssetQueue textureQueue) {
        GdxMeshes meshes = new GdxMeshes(stubMeshOpener());
        return newAssetsForTest(textures, meshes, textureQueue, idleMeshQueue());
    }

    private static GdxMeshes.MeshOpener stubMeshOpener() {
        return new GdxMeshes.MeshOpener() {
            @Override
            public GdxMeshes.LoadedMesh open(String path) {
                return null;
            }
        };
    }

    static GdxAssetManagerAssets.MeshAssetQueue idleMeshQueue() {
        return new GdxAssetManagerAssets.MeshAssetQueue() {
            @Override
            public void loadMesh(String path) {}

            @Override
            public boolean update() {
                return true;
            }

            @Override
            public float getProgress() {
                return 0f;
            }

            @Override
            public boolean isMeshLoaded(String path) {
                return false;
            }

            @Override
            public GdxMeshes.LoadedMesh takeMeshOwnership(String path) {
                return null;
            }

            @Override
            public void setErrorListener(GdxAssetManagerAssets.QueueErrorListener listener) {}

            @Override
            public void cancel(String path) {}

            @Override
            public void dispose() {}
        };
    }

    private static LibGdxAssetQueues libGdxAssetQueues() {
        final AssetManager assetManager = new AssetManager();
        assetManager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assetManager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());
        return new LibGdxAssetQueues(assetManager);
    }

    /**
     * Single AssetManager facade for TEXTURE and MESH so one {@code update()} per pump satisfies
     * the shared async contract.
     */
    private static final class LibGdxAssetQueues
            implements GdxAssetManagerAssets.TextureAssetQueue,
                    GdxAssetManagerAssets.MeshAssetQueue {
        private final AssetManager assetManager;
        private GdxAssetManagerAssets.QueueErrorListener listener;

        LibGdxAssetQueues(AssetManager assetManager) {
            this.assetManager = assetManager;
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
        public void loadMesh(String path) {
            assetManager.load(path, SceneAsset.class);
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
        public boolean isMeshLoaded(String path) {
            return assetManager.isLoaded(path, SceneAsset.class);
        }

        @Override
        public GdxMeshes.LoadedMesh takeMeshOwnership(String path) {
            final SceneAsset sceneAsset = assetManager.get(path, SceneAsset.class);
            return GdxMeshes.fromSceneAsset(sceneAsset);
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
    }
}