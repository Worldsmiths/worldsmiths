package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import java.util.ArrayList;
import java.util.List;

/** Mutable {@link LoadHandle} shared by GDX async asset backends. Package-private. */
final class GdxLoadHandle implements LoadHandle {
    final String path;
    final AssetKind kind;
    LoadState state;
    float progress;
    String failureMessage;
    private final List<Runnable> terminalListeners = new ArrayList<Runnable>();
    private boolean terminalFired;

    GdxLoadHandle(String path, AssetKind kind) {
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
            listener.run();
        }
        terminalListeners.clear();
    }
}
