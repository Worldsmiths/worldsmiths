package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recording {@link Scenes} for headless tests. Assert command kinds ({@code LOAD}, {@code SWITCH},
 * {@code UNLOAD}, {@code EXIT}, {@code ENTER}, {@code FAIL_KEEP_PREVIOUS}) without a display.
 *
 * <p>Delegates path decode, Sprite key preload, and replace/unload to {@link DefaultScenes} while
 * recording lifecycle for Fake oracles.
 */
public final class FakeScenes implements Scenes {
    private final DefaultScenes delegate;
    private final List<Command> commands = new ArrayList<Command>();

    public FakeScenes(Files files, Textures textures) {
        this.delegate =
                new DefaultScenes(
                        files,
                        textures,
                        new DefaultScenes.LifecycleListener() {
                            @Override
                            public void onExit(Scene scene) {
                                commands.add(Command.exit(scene));
                            }

                            @Override
                            public void onEnter(Scene scene) {
                                commands.add(Command.enter(scene));
                            }
                        });
    }

    @Override
    public void setRegistry(ComponentRegistry registry) {
        delegate.setRegistry(registry);
    }

    @Override
    public void load(String path) {
        commands.add(Command.load(path));
        try {
            delegate.load(path);
        } catch (EngineException e) {
            commands.add(Command.failKeepPrevious(path));
            throw e;
        }
    }

    @Override
    public void switchTo(Scene scene) {
        commands.add(Command.switchTo(scene));
        delegate.switchTo(scene);
    }

    @Override
    public void unload() {
        commands.add(Command.unload());
        delegate.unload();
    }

    @Override
    public Scene current() {
        return delegate.current();
    }

    @Override
    public void addTeardownHook(SceneTeardownHook hook) {
        delegate.addTeardownHook(hook);
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void clear() {
        commands.clear();
    }

    /** One recorded Scenes lifecycle call. */
    public static final class Command {
        public enum Kind {
            LOAD,
            SWITCH,
            UNLOAD,
            EXIT,
            ENTER,
            FAIL_KEEP_PREVIOUS
        }

        public final Kind kind;
        public final String path;
        public final Scene scene;

        private Command(Kind kind, String path, Scene scene) {
            this.kind = kind;
            this.path = path;
            this.scene = scene;
        }

        static Command load(String path) {
            return new Command(Kind.LOAD, path, null);
        }

        static Command switchTo(Scene scene) {
            return new Command(Kind.SWITCH, null, scene);
        }

        static Command unload() {
            return new Command(Kind.UNLOAD, null, null);
        }

        static Command exit(Scene scene) {
            return new Command(Kind.EXIT, null, scene);
        }

        static Command enter(Scene scene) {
            return new Command(Kind.ENTER, null, scene);
        }

        static Command failKeepPrevious(String path) {
            return new Command(Kind.FAIL_KEEP_PREVIOUS, path, null);
        }

        @Override
        public String toString() {
            return kind
                    + (path != null ? "(" + path + ")" : "")
                    + (scene != null && scene.name != null ? "[" + scene.name + "]" : "");
        }
    }
}
