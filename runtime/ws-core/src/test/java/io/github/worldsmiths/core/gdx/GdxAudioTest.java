package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Sound;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Headless GdxAudio coverage via an injected {@link GdxAudio.SoundOpener} — no Application /
 * display / audio device required.
 */
class GdxAudioTest {

    @Test
    void createsPlaysStopsAndDisposesViaInjectedOpener() {
        RecordingGdxSound gdxSound = new RecordingGdxSound();
        Map<String, com.badlogic.gdx.audio.Sound> catalog =
                new HashMap<String, com.badlogic.gdx.audio.Sound>();
        catalog.put("audio/cue.wav", gdxSound);

        GdxAudio audio =
                new GdxAudio(
                        new GdxAudio.SoundOpener() {
                            @Override
                            public com.badlogic.gdx.audio.Sound open(String path) {
                                return catalog.get(path);
                            }
                        });

        Sound sound = audio.createSound("audio/cue.wav");
        sound.play();
        sound.stop();
        sound.dispose();

        assertEquals(
                java.util.Arrays.asList("play", "stop", "dispose"), gdxSound.calls);
    }

    @Test
    void missingInternalPathThrowsEngineException() {
        GdxAudio audio =
                new GdxAudio(
                        new GdxAudio.SoundOpener() {
                            @Override
                            public com.badlogic.gdx.audio.Sound open(String path) {
                                return null;
                            }
                        });

        EngineException thrown =
                assertThrows(EngineException.class, () -> audio.createSound("gone.wav"));
        assertTrue(thrown.getMessage().contains("gone.wav"));
    }

    @Test
    void unloadableOpenThrowsEngineException() {
        GdxAudio audio =
                new GdxAudio(
                        new GdxAudio.SoundOpener() {
                            @Override
                            public com.badlogic.gdx.audio.Sound open(String path) {
                                throw new IllegalStateException("decode failed");
                            }
                        });

        EngineException thrown =
                assertThrows(EngineException.class, () -> audio.createSound("bad.wav"));
        assertTrue(thrown.getMessage().contains("bad.wav"));
        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }

    @Test
    void playAndStopSwallowDelegateFailures() {
        FailingGdxSound gdxSound = new FailingGdxSound();
        GdxAudio audio =
                new GdxAudio(
                        new GdxAudio.SoundOpener() {
                            @Override
                            public com.badlogic.gdx.audio.Sound open(String path) {
                                return gdxSound;
                            }
                        });

        Sound sound = audio.createSound("audio/cue.wav");
        sound.play();
        sound.stop();
        assertEquals(2, gdxSound.attempts);
    }

    @Test
    void disposeIsIdempotentAndCallsDelegateOnce() {
        RecordingGdxSound gdxSound = new RecordingGdxSound();
        GdxAudio audio =
                new GdxAudio(
                        new GdxAudio.SoundOpener() {
                            @Override
                            public com.badlogic.gdx.audio.Sound open(String path) {
                                return gdxSound;
                            }
                        });

        Sound sound = audio.createSound("audio/cue.wav");
        sound.dispose();
        sound.dispose();

        assertEquals(java.util.Arrays.asList("dispose"), gdxSound.calls);
    }

    private static final class RecordingGdxSound implements com.badlogic.gdx.audio.Sound {
        final List<String> calls = new ArrayList<String>();

        @Override
        public long play() {
            calls.add("play");
            return 1L;
        }

        @Override
        public long play(float volume) {
            return play();
        }

        @Override
        public long play(float volume, float pitch, float pan) {
            return play();
        }

        @Override
        public long loop() {
            return 0L;
        }

        @Override
        public long loop(float volume) {
            return 0L;
        }

        @Override
        public long loop(float volume, float pitch, float pan) {
            return 0L;
        }

        @Override
        public void stop() {
            calls.add("stop");
        }

        @Override
        public void stop(long soundId) {
            stop();
        }

        @Override
        public void pause() {}

        @Override
        public void pause(long soundId) {}

        @Override
        public void resume() {}

        @Override
        public void resume(long soundId) {}

        @Override
        public void setLooping(long soundId, boolean looping) {}

        @Override
        public void setPitch(long soundId, float pitch) {}

        @Override
        public void setVolume(long soundId, float volume) {}

        @Override
        public void setPan(long soundId, float pan, float volume) {}

        @Override
        public void dispose() {
            calls.add("dispose");
        }
    }

    private static final class FailingGdxSound implements com.badlogic.gdx.audio.Sound {
        int attempts;

        @Override
        public long play() {
            attempts++;
            throw new IllegalStateException("device busy");
        }

        @Override
        public long play(float volume) {
            return play();
        }

        @Override
        public long play(float volume, float pitch, float pan) {
            return play();
        }

        @Override
        public long loop() {
            return 0L;
        }

        @Override
        public long loop(float volume) {
            return 0L;
        }

        @Override
        public long loop(float volume, float pitch, float pan) {
            return 0L;
        }

        @Override
        public void stop() {
            attempts++;
            throw new IllegalStateException("device busy");
        }

        @Override
        public void stop(long soundId) {
            stop();
        }

        @Override
        public void pause() {}

        @Override
        public void pause(long soundId) {}

        @Override
        public void resume() {}

        @Override
        public void resume(long soundId) {}

        @Override
        public void setLooping(long soundId, boolean looping) {}

        @Override
        public void setPitch(long soundId, float pitch) {}

        @Override
        public void setVolume(long soundId, float volume) {}

        @Override
        public void setPan(long soundId, float pan, float volume) {}

        @Override
        public void dispose() {}
    }
}
