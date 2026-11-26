package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recording {@link Assets} for headless tests. Advances only on explicit {@link #pump()} / script —
 * never {@code Thread.sleep}. Seed TEXTURE paths with {@link #putTexture}; optional {@link
 * #script(String, float...)} for discrete loading steps; assert command kinds ({@code REQUEST},
 * {@code PROGRESS}, {@code COMPLETE}, {@code FAIL}, {@code LISTENER}, {@code UPDATE}).
 *
 * <p>When constructed with a {@link FakeTextures} (via {@link FakeEngineServices}), COMPLETE merges
 * into that texture cache so {@code textures().resolve} is a hit without a second CREATE.
 */
public final class FakeAssets implements Assets {
    private final Set<String> availableTextures = new HashSet<String>();
    private final Map<String, float[]> scripts = new HashMap<String, float[]>();
    private final Map<String, FakeLoadHandle> handlesByKey = new HashMap<String, FakeLoadHandle>();
    private final List<Command> commands = new ArrayList<Command>();
    private final List<FakeLoadHandle> inFlight = new ArrayList<FakeLoadHandle>();
    private final FakeTextures textures;

    /** Standalone FakeAssets with no texture-cache merge. */
    public FakeAssets() {
        this(null);
    }

    /**
     * Wire to a {@link FakeTextures} so TEXTURE COMPLETE merges into the same sync cache (AC4 Fake
     * path).
     */
    public FakeAssets(FakeTextures textures) {
        this.textures = textures;
    }

    /** Marks an internal path as a loadable TEXTURE for later {@link #request} calls. */
    public void putTexture(String internalPath) {
        availableTextures.add(internalPath);
        if (textures != null) {
            textures.putTexture(internalPath);
        }
    }

    /**
     * Scripts discrete LOADING progress values before COMPLETE for a seeded TEXTURE path. Each
     * {@link #pump()} consumes one progress value (state {@link LoadState#LOADING}); a further
     * pump reaches COMPLETE. Without a script, a seeded path completes on the first pump.
     */
    public void script(String internalPath, float... loadingProgress) {
        float[] copy = new float[loadingProgress.length];
        System.arraycopy(loadingProgress, 0, copy, 0, loadingProgress.length);
        scripts.put(internalPath, copy);
    }

    @Override
    public LoadHandle request(String path, AssetKind kind) {
        String key = handleKey(kind, path);
        FakeLoadHandle existing = handlesByKey.get(key);
        if (existing != null) {
            return existing;
        }
        FakeLoadHandle handle = new FakeLoadHandle(path, kind);
        commands.add(Command.request(path, kind));
        handlesByKey.put(key, handle);
        if (path == null || path.trim().isEmpty()) {
            failHandleImmediate(handle, "Asset path is null or blank");
            return handle;
        }
        if (kind == AssetKind.MESH || kind == AssetKind.MUSIC) {
            failHandleImmediate(handle, "Unsupported asset kind: " + kind);
            return handle;
        }
        handle.state = LoadState.QUEUED;
        handle.progress = 0f;
        float[] script = scripts.get(path);
        if (script != null) {
            handle.scriptSteps = script;
        }
        inFlight.add(handle);
        return handle;
    }

    private void failHandleImmediate(FakeLoadHandle handle, String message) {
        handle.state = LoadState.FAILED;
        handle.progress = 0f;
        handle.failureMessage = message;
        commands.add(Command.fail(handle.path, handle.kind, message));
        handle.fireTerminal();
    }

    private static String handleKey(AssetKind kind, String path) {
        return kind + "\0" + path;
    }

    /**
     * Advances in-flight loads by one discrete step. Tests must call this explicitly — no sleeps.
     */
    public void pump() {
        List<FakeLoadHandle> snapshot = new ArrayList<FakeLoadHandle>(inFlight);
        for (FakeLoadHandle handle : snapshot) {
            if (handle.state == LoadState.COMPLETE || handle.state == LoadState.FAILED) {
                continue;
            }
            if (handle.kind == AssetKind.TEXTURE && availableTextures.contains(handle.path)) {
                advanceSeededTexture(handle);
            } else if (handle.kind == AssetKind.TEXTURE) {
                failHandle(handle, "Missing or unloadable texture: " + handle.path);
            }
        }
    }

    private void advanceSeededTexture(FakeLoadHandle handle) {
        if (handle.scriptSteps != null && handle.scriptIndex < handle.scriptSteps.length) {
            float value = handle.scriptSteps[handle.scriptIndex++];
            handle.state = LoadState.LOADING;
            handle.progress = value;
            commands.add(Command.progress(handle.path, handle.kind, value));
            return;
        }
        handle.state = LoadState.COMPLETE;
        handle.progress = 1f;
        if (textures != null && handle.kind == AssetKind.TEXTURE) {
            textures.cacheFromAsync(handle.path);
        }
        commands.add(Command.complete(handle.path, handle.kind));
        inFlight.remove(handle);
        handle.fireTerminal();
    }

    private void failHandle(FakeLoadHandle handle, String message) {
        handle.state = LoadState.FAILED;
        handle.progress = 0f;
        handle.failureMessage = message;
        commands.add(Command.fail(handle.path, handle.kind, message));
        inFlight.remove(handle);
        handle.fireTerminal();
    }

    /**
     * Harness marker recorded as {@link Command.Kind#UPDATE} so tests can assert listener order
     * after pump and before a Fake "game.update" phase.
     */
    public void markUpdate() {
        commands.add(Command.update());
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void clear() {
        commands.clear();
    }

    private final class FakeLoadHandle implements LoadHandle {
        private final String path;
        private final AssetKind kind;
        private LoadState state;
        private float progress;
        private String failureMessage;
        private float[] scriptSteps;
        private int scriptIndex;
        private final List<Runnable> terminalListeners = new ArrayList<Runnable>();
        private boolean terminalFired;

        FakeLoadHandle(String path, AssetKind kind) {
            this.path = path;
            this.kind = kind;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public AssetKind kind() {
            return kind;
        }

        @Override
        public LoadState state() {
            return state;
        }

        @Override
        public float progress() {
            return progress;
        }

        @Override
        public String failureMessage() {
            return failureMessage;
        }

        @Override
        public void onTerminal(Runnable listener) {
            if (listener == null) {
                return;
            }
            if (terminalFired) {
                listener.run();
                return;
            }
            terminalListeners.add(listener);
        }

        void fireTerminal() {
            if (terminalFired) {
                return;
            }
            terminalFired = true;
            for (Runnable listener : terminalListeners) {
                commands.add(Command.listener(path, kind));
                listener.run();
            }
            terminalListeners.clear();
        }
    }

    /** One recorded Assets / harness event. */
    public static final class Command {
        public enum Kind {
            REQUEST,
            PROGRESS,
            COMPLETE,
            FAIL,
            LISTENER,
            UPDATE
        }

        public final Kind kind;
        public final String path;
        public final AssetKind assetKind;
        public final float progress;
        public final String message;

        private Command(
                Kind kind, String path, AssetKind assetKind, float progress, String message) {
            this.kind = kind;
            this.path = path;
            this.assetKind = assetKind;
            this.progress = progress;
            this.message = message;
        }

        static Command request(String path, AssetKind assetKind) {
            return new Command(Kind.REQUEST, path, assetKind, 0f, null);
        }

        static Command progress(String path, AssetKind assetKind, float progress) {
            return new Command(Kind.PROGRESS, path, assetKind, progress, null);
        }

        static Command complete(String path, AssetKind assetKind) {
            return new Command(Kind.COMPLETE, path, assetKind, 1f, null);
        }

        static Command fail(String path, AssetKind assetKind, String message) {
            return new Command(Kind.FAIL, path, assetKind, 0f, message);
        }

        static Command listener(String path, AssetKind assetKind) {
            return new Command(Kind.LISTENER, path, assetKind, 0f, null);
        }

        static Command update() {
            return new Command(Kind.UPDATE, null, null, 0f, null);
        }
    }
}
