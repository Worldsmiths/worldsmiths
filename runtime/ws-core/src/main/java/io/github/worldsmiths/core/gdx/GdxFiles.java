package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Files;

/**
 * GDX-backed {@link Files} reading {@code Gdx.files.internal} only. Not part of the author-facing
 * API — backends assemble this via {@link GdxEngineServices}.
 */
final class GdxFiles implements Files {
    private final InternalOpener opener;

    GdxFiles() {
        this(
                new InternalOpener() {
                    @Override
                    public FileHandle open(String path) {
                        return Gdx.files.internal(path);
                    }
                });
    }

    /** Package-visible for tests that inject handles without a live Application / display. */
    GdxFiles(InternalOpener opener) {
        this.opener = opener;
    }

    @Override
    public String readString(String path) {
        FileHandle handle = requireExists(path);
        try {
            return handle.readString("UTF-8");
        } catch (RuntimeException e) {
            throw new EngineException("Failed to read internal file: " + path, e);
        }
    }

    @Override
    public byte[] readBytes(String path) {
        FileHandle handle = requireExists(path);
        try {
            return handle.readBytes();
        } catch (RuntimeException e) {
            throw new EngineException("Failed to read internal file: " + path, e);
        }
    }

    private FileHandle requireExists(String path) {
        FileHandle handle = opener.open(path);
        if (handle == null || !handle.exists()) {
            throw new EngineException("Missing internal file: " + path);
        }
        return handle;
    }

    /** Resolves an internal path to a {@link FileHandle}. */
    interface InternalOpener {
        FileHandle open(String path);
    }
}
