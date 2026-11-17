package io.github.worldsmiths.ios;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.ApplicationListener;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;
import org.junit.jupiter.api.Test;

class IosBackendTest {

    @Test
    void attachRejectsNullApplication() {
        NullPointerException thrown =
                assertThrows(
                        NullPointerException.class,
                        () -> IosBackend.attach(null, new StubGame(), new IosConfig()));
        assertTrue("application".equals(thrown.getMessage()), thrown.getMessage());
    }

    @Test
    void attachRejectsNullGame() {
        NullPointerException thrown =
                assertThrows(
                        NullPointerException.class,
                        () -> IosBackend.attach(new RecordingHost(), null, new IosConfig()));
        assertTrue("game".equals(thrown.getMessage()), thrown.getMessage());
    }

    @Test
    void attachRejectsNullConfig() {
        NullPointerException thrown =
                assertThrows(
                        NullPointerException.class,
                        () -> IosBackend.attach(new RecordingHost(), new StubGame(), null));
        assertTrue("config".equals(thrown.getMessage()), thrown.getMessage());
    }

    @Test
    void attachInitializesHostWithGameListenerAndSameConfig() {
        RecordingHost host = new RecordingHost();
        StubGame game = new StubGame();
        IosConfig config = new IosConfig(false, true, false, true, false);

        IosBackend.attach(host, game, config);

        assertTrue(host.listener instanceof IosGameListener);
        assertSame(config, host.config);
    }

    private static final class RecordingHost implements IosApplicationHost {
        ApplicationListener listener;
        IosConfig config;

        @Override
        public void initialize(ApplicationListener listener, IosConfig config) {
            this.listener = listener;
            this.config = config;
        }
    }

    private static final class StubGame implements WorldsmithsGame {
        @Override
        public Scene createScene() {
            return new Scene("stub");
        }

        @Override
        public void create() {}

        @Override
        public void update(float delta) {}

        @Override
        public void render() {}

        @Override
        public void dispose() {}
    }
}
