package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recording {@link Audio} for headless tests. Seed available paths with {@link #putSound}, then
 * assert command kinds ({@code CREATE_SOUND}, {@code PLAY}, {@code STOP}, {@code DISPOSE}) without
 * a display or audio device.
 */
public final class FakeAudio implements Audio {
    private final Set<String> available = new HashSet<String>();
    private final List<Command> commands = new ArrayList<Command>();

    /** Marks an internal path as loadable for later {@link #createSound} calls. */
    public void putSound(String internalPath) {
        available.add(internalPath);
    }

    @Override
    public Sound createSound(String internalPath) {
        if (!available.contains(internalPath)) {
            throw new EngineException("Missing or unloadable sound: " + internalPath);
        }
        commands.add(Command.createSound(internalPath));
        return new FakeSound(internalPath);
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void clear() {
        commands.clear();
    }

    private final class FakeSound implements Sound {
        private final String path;
        private boolean disposed;

        FakeSound(String path) {
            this.path = path;
        }

        @Override
        public void play() {
            commands.add(Command.play(path));
        }

        @Override
        public void stop() {
            commands.add(Command.stop(path));
        }

        @Override
        public void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            commands.add(Command.dispose(path));
        }
    }

    /** One recorded Audio / Sound call. */
    public static final class Command {
        public enum Kind {
            CREATE_SOUND,
            PLAY,
            STOP,
            DISPOSE
        }

        public final Kind kind;
        /** Internal path for create / play / stop / dispose. */
        public final String path;

        private Command(Kind kind, String path) {
            this.kind = kind;
            this.path = path;
        }

        static Command createSound(String path) {
            return new Command(Kind.CREATE_SOUND, path);
        }

        static Command play(String path) {
            return new Command(Kind.PLAY, path);
        }

        static Command stop(String path) {
            return new Command(Kind.STOP, path);
        }

        static Command dispose(String path) {
            return new Command(Kind.DISPOSE, path);
        }
    }
}
