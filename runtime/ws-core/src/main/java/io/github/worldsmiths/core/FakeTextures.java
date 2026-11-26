package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recording {@link Textures} for headless tests. Seed available paths with {@link #putTexture}, then
 * assert command kinds ({@code CREATE}, {@code PRELOAD}, {@code RESOLVE}) without a display or GPU.
 */
public final class FakeTextures implements Textures {
    private final Set<String> available = new HashSet<String>();
    private final Set<String> cached = new HashSet<String>();
    private final Set<String> asyncInFlight = new HashSet<String>();
    private final List<Command> commands = new ArrayList<Command>();

    /** Marks an internal path as loadable for later {@link #preload} / {@link #resolve} calls. */
    public void putTexture(String internalPath) {
        available.add(internalPath);
    }

    /**
     * Merges an async TEXTURE complete into the sync cache (records {@code CREATE} once). Used by
     * {@link FakeAssets} so {@link #resolve} after COMPLETE is a cache hit. Clears the in-flight
     * mark for {@code internalPath}.
     */
    public void cacheFromAsync(String internalPath) {
        available.add(internalPath);
        asyncInFlight.remove(internalPath);
        if (cached.add(internalPath)) {
            commands.add(Command.create(internalPath));
        }
    }

    /** Marks {@code internalPath} as async TEXTURE in flight (wired from {@link FakeAssets}). */
    public void beginAsyncLoad(String internalPath) {
        asyncInFlight.add(internalPath);
    }

    /** Clears the async in-flight mark for {@code internalPath} (COMPLETE or FAIL). */
    public void endAsyncLoad(String internalPath) {
        asyncInFlight.remove(internalPath);
    }

    @Override
    public void preload(String internalPath) {
        ensureCached(internalPath);
        commands.add(Command.preload(internalPath));
    }

    @Override
    public void resolve(String internalPath) {
        ensureCached(internalPath);
        commands.add(Command.resolve(internalPath));
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void clear() {
        commands.clear();
    }

    private void ensureCached(String internalPath) {
        if (cached.contains(internalPath)) {
            return;
        }
        if (asyncInFlight.contains(internalPath)) {
            throw new EngineException(
                    "Async load in flight for path; cannot sync-open: " + internalPath);
        }
        if (!available.contains(internalPath)) {
            throw new EngineException("Missing or unloadable texture: " + internalPath);
        }
        if (cached.add(internalPath)) {
            commands.add(Command.create(internalPath));
        }
    }

    /** One recorded Textures call. */
    public static final class Command {
        public enum Kind {
            CREATE,
            PRELOAD,
            RESOLVE
        }

        public final Kind kind;
        /** Internal path for create / preload / resolve. */
        public final String path;

        private Command(Kind kind, String path) {
            this.kind = kind;
            this.path = path;
        }

        static Command create(String path) {
            return new Command(Kind.CREATE, path);
        }

        static Command preload(String path) {
            return new Command(Kind.PRELOAD, path);
        }

        static Command resolve(String path) {
            return new Command(Kind.RESOLVE, path);
        }
    }
}
