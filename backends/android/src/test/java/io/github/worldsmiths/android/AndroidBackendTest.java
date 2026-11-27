package io.github.worldsmiths.android;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.ApplicationListener;
import io.github.worldsmiths.core.WorldsmithsGame;
import org.junit.jupiter.api.Test;

class AndroidBackendTest {

    @Test
    void attachRejectsNullApplication() {
        assertThrows(
                NullPointerException.class,
                () -> AndroidBackend.attach(null, new StubGame(), new AndroidConfig()));
    }

    @Test
    void attachRejectsNullGame() {
        assertThrows(
                NullPointerException.class,
                () -> AndroidBackend.attach(new RecordingHost(), null, new AndroidConfig()));
    }

    @Test
    void attachRejectsNullConfig() {
        assertThrows(
                NullPointerException.class,
                () -> AndroidBackend.attach(new RecordingHost(), new StubGame(), null));
    }

    @Test
    void attachInitializesHostWithGameListenerAndSameConfig() {
        RecordingHost host = new RecordingHost();
        StubGame game = new StubGame();
        AndroidConfig config = new AndroidConfig(false, true, false, true, false);

        AndroidBackend.attach(host, game, config);

        assertTrue(host.listener instanceof AndroidGameListener);
        assertSame(config, host.config);
    }

    private static final class RecordingHost implements AndroidApplicationHost {
        ApplicationListener listener;
        AndroidConfig config;

        @Override
        public void initialize(ApplicationListener listener, AndroidConfig config) {
            this.listener = listener;
            this.config = config;
        }
    }

    private static final class StubGame implements WorldsmithsGame {
        @Override
        public void create(io.github.worldsmiths.core.EngineServices services) {}

        @Override
        public void update(float delta) {}

        @Override
        public void render() {}

        @Override
        public void dispose() {}
    }
}
