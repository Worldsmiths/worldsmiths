package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * Private path→mesh registry for async MESH loads and later {@code createModelFromLoaded} bind
 * (T4). Cooperative {@link GdxAssets} and JVM {@link GdxAssetManagerAssets} merge completes here.
 * Not part of the author-facing API.
 */
final class GdxMeshes {
    private final Map<String, LoadedMesh> cache = new HashMap<String, LoadedMesh>();
    private final Set<String> asyncInFlight = new HashSet<String>();
    private final Set<String> externallyOwned = new HashSet<String>();
    private final MeshOpener opener;

    GdxMeshes() {
        this(new ProductionMeshOpener());
    }

    /** Package-visible for headless tests that inject meshes without GLTF / GL. */
    GdxMeshes(MeshOpener opener) {
        this.opener = opener;
    }

    LoadedMesh getIfPresent(String internalPath) {
        return cache.get(internalPath);
    }

    /** Whether async MESH for {@code internalPath} has merged into the registry and can bind. */
    boolean isMeshLoadedForBind(String internalPath) {
        return cache.containsKey(internalPath);
    }

    com.badlogic.gdx.graphics.g3d.Model gdxModelForBind(String internalPath) {
        LoadedMesh loaded = cache.get(internalPath);
        if (loaded == null) {
            return null;
        }
        return loaded.gdxModel();
    }

    boolean isAsyncInFlight(String internalPath) {
        return asyncInFlight.contains(internalPath);
    }

    void beginAsyncLoad(String internalPath) {
        asyncInFlight.add(internalPath);
    }

    void endAsyncLoad(String internalPath) {
        asyncInFlight.remove(internalPath);
    }

    LoadedMesh openForAsync(String internalPath) {
        return opener.open(internalPath);
    }

    void putFromAsync(String internalPath, LoadedMesh loaded) {
        if (loaded == null) {
            return;
        }
        if (!cache.containsKey(internalPath)) {
            cache.put(internalPath, loaded);
        }
        asyncInFlight.remove(internalPath);
    }

    void putFromAsyncManagedExternally(String internalPath, LoadedMesh loaded) {
        if (loaded == null) {
            return;
        }
        if (!cache.containsKey(internalPath)) {
            cache.put(internalPath, loaded);
            externallyOwned.add(internalPath);
        }
        asyncInFlight.remove(internalPath);
    }

    void evictWithoutDispose(String internalPath) {
        cache.remove(internalPath);
        externallyOwned.remove(internalPath);
        asyncInFlight.remove(internalPath);
    }

    void dispose() {
        for (Map.Entry<String, LoadedMesh> entry : cache.entrySet()) {
            if (!externallyOwned.contains(entry.getKey())) {
                entry.getValue().dispose();
            }
        }
        cache.clear();
        externallyOwned.clear();
        asyncInFlight.clear();
    }

    int cacheSizeForTest() {
        return cache.size();
    }

    static LoadedMesh fromSceneAsset(final SceneAsset sceneAsset) {
        return new LoadedMesh() {
            @Override
            public Model gdxModel() {
                return sceneAsset.scene.model;
            }

            @Override
            public void dispose() {
                sceneAsset.dispose();
            }
        };
    }

    /**
     * Resolves an internal glTF/GLB path to a loaded mesh. Return {@code null} when missing; throw
     * on unloadable decode failures.
     */
    interface MeshOpener {
        LoadedMesh open(String path);
    }

    /** GPU mesh lifetime seam for tests and registry merge. */
    interface LoadedMesh {
        Model gdxModel();

        void dispose();
    }

    private static final class ProductionMeshOpener implements MeshOpener {
        @Override
        public LoadedMesh open(String path) {
            FileHandle file = Gdx.files.internal(path);
            if (file == null || !file.exists()) {
                return null;
            }
            String ext = file.extension().toLowerCase();
            SceneAsset sceneAsset;
            try {
                if ("glb".equals(ext)) {
                    sceneAsset = new GLBLoader().load(file);
                } else if ("gltf".equals(ext)) {
                    sceneAsset = new GLTFLoader().load(file);
                } else {
                    return null;
                }
            } catch (RuntimeException e) {
                throw e;
            }
            if (sceneAsset == null || sceneAsset.scene == null || sceneAsset.scene.model == null) {
                return null;
            }
            return fromSceneAsset(sceneAsset);
        }
    }
}