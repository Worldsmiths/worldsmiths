package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.github.worldsmiths.core.Audio;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Sound;

/**
 * GDX-backed {@link Audio} loading one-shot sounds via {@code Gdx.audio} + internal files. Not part
 * of the author-facing API — backends assemble this via {@link GdxEngineServices}.
 */
final class GdxAudio implements Audio {
    private final SoundOpener opener;

    GdxAudio() {
        this(
                new SoundOpener() {
                    @Override
                    public com.badlogic.gdx.audio.Sound open(String path) {
                        FileHandle handle = Gdx.files.internal(path);
                        if (handle == null || !handle.exists()) {
                            return null;
                        }
                        return Gdx.audio.newSound(handle);
                    }
                });
    }

    /** Package-visible for tests that inject sounds without a live Application / audio device. */
    GdxAudio(SoundOpener opener) {
        this.opener = opener;
    }

    @Override
    public Sound createSound(String internalPath) {
        com.badlogic.gdx.audio.Sound gdxSound;
        try {
            gdxSound = opener.open(internalPath);
        } catch (RuntimeException e) {
            throw new EngineException("Failed to load sound: " + internalPath, e);
        }
        if (gdxSound == null) {
            throw new EngineException("Missing or unloadable sound: " + internalPath);
        }
        return new GdxSound(gdxSound);
    }

    /**
     * Resolves an internal path to a loaded LibGDX sound. Return {@code null} when the path is
     * missing; throw on unloadable decode failures.
     */
    interface SoundOpener {
        com.badlogic.gdx.audio.Sound open(String path);
    }

    private static final class GdxSound implements Sound {
        private final com.badlogic.gdx.audio.Sound delegate;

        GdxSound(com.badlogic.gdx.audio.Sound delegate) {
            this.delegate = delegate;
        }

        @Override
        public void play() {
            try {
                delegate.play();
            } catch (RuntimeException ignored) {
                // best-effort — never fail the game
            }
        }

        @Override
        public void stop() {
            try {
                delegate.stop();
            } catch (RuntimeException ignored) {
                // best-effort — never fail the game
            }
        }

        @Override
        public void dispose() {
            delegate.dispose();
        }
    }
}
