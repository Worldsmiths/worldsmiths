package io.github.worldsmiths.core;

import com.badlogic.gdx.utils.SerializationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Production {@link Scenes} implementation: Files → SceneCodec → texture preload → exit/enter. Used
 * by GDX service bags; headless tests use {@link FakeScenes}.
 */
public final class DefaultScenes implements Scenes {
    private final Files files;
    private final Textures textures;
    private final LifecycleListener lifecycle;
    private final List<SceneTeardownHook> teardownHooks = new ArrayList<SceneTeardownHook>();
    private ComponentRegistry registry = ComponentRegistries.builtins();
    private Scene current;

    public DefaultScenes(Files files, Textures textures) {
        this(files, textures, LifecycleListener.NOOP);
    }

    DefaultScenes(Files files, Textures textures, LifecycleListener lifecycle) {
        if (files == null) {
            throw new NullPointerException("files");
        }
        if (textures == null) {
            throw new NullPointerException("textures");
        }
        if (lifecycle == null) {
            throw new NullPointerException("lifecycle");
        }
        this.files = files;
        this.textures = textures;
        this.lifecycle = lifecycle;
    }

    @Override
    public void setRegistry(ComponentRegistry registry) {
        if (registry == null) {
            throw new NullPointerException("registry");
        }
        this.registry = registry;
    }

    @Override
    public void load(String path) {
        Scene decoded = decodePath(path);
        try {
            preloadDistinctSpriteKeys(decoded);
        } catch (EngineException e) {
            throw wrapLoadFailure(path, e);
        } catch (RuntimeException e) {
            throw wrapLoadFailure(path, e);
        }
        replaceCurrent(decoded);
    }

    @Override
    public void switchTo(Scene scene) {
        if (scene == null) {
            throw new NullPointerException("scene");
        }
        replaceCurrent(scene);
    }

    @Override
    public void unload() {
        exitCurrent();
        current = null;
    }

    @Override
    public Scene current() {
        return current;
    }

    @Override
    public void addTeardownHook(SceneTeardownHook hook) {
        if (hook == null) {
            throw new NullPointerException("hook");
        }
        teardownHooks.add(hook);
    }

    private Scene decodePath(String path) {
        try {
            String json = files.readString(path);
            return SceneCodec.create(registry).fromJson(json);
        } catch (EngineException e) {
            throw wrapLoadFailure(path, e);
        } catch (SerializationException e) {
            throw wrapLoadFailure(path, e);
        } catch (RuntimeException e) {
            throw wrapLoadFailure(path, e);
        }
    }

    private static EngineException wrapLoadFailure(String path, RuntimeException cause) {
        if (cause instanceof EngineException
                && cause.getMessage() != null
                && cause.getMessage().contains(String.valueOf(path))) {
            return (EngineException) cause;
        }
        return new EngineException("Failed to load scene: " + path, cause);
    }

    private void replaceCurrent(Scene next) {
        exitCurrent();
        current = next;
        lifecycle.onEnter(next);
    }

    private void exitCurrent() {
        if (current == null) {
            return;
        }
        Scene leaving = current;
        lifecycle.onExit(leaving);
        for (int i = 0; i < teardownHooks.size(); i++) {
            teardownHooks.get(i).onTeardown(leaving);
        }
    }

    private void preloadDistinctSpriteKeys(Scene scene) {
        Set<String> keys = new HashSet<String>();
        collectTextureKeys(scene.nodes, keys);
        for (String key : keys) {
            textures.preload(key);
        }
    }

    private static void collectTextureKeys(Iterable<Node> nodes, Set<String> keys) {
        for (Node node : nodes) {
            Sprite sprite = node.get(Sprite.class);
            if (sprite != null && sprite.texture != null && !sprite.texture.trim().isEmpty()) {
                keys.add(sprite.texture);
            }
            collectTextureKeys(node.children, keys);
        }
    }

    /** Package-visible recording hook used by {@link FakeScenes}. */
    interface LifecycleListener {
        LifecycleListener NOOP =
                new LifecycleListener() {
                    @Override
                    public void onExit(Scene scene) {}

                    @Override
                    public void onEnter(Scene scene) {}
                };

        void onExit(Scene scene);

        void onEnter(Scene scene);
    }
}
