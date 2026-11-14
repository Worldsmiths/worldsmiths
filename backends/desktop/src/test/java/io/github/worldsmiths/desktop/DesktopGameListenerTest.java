package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopGameListenerTest {

    @Test
    void createCallsCreateSceneThenCreateAndRetainsReturnedScene() {
        RecordingGame game = new RecordingGame();
        DesktopGameListener listener = new DesktopGameListener(game);

        listener.create();

        assertEquals(Arrays.asList("createScene", "create"), game.calls);
        assertSame(game.scene, listener.retainedScene());
    }

    @Test
    void updateAndRenderForwardToTheGameWithoutStartingLwjgl() {
        RecordingGame game = new RecordingGame();
        DesktopGameListener listener = new DesktopGameListener(game);

        listener.updateAndRender(0.016f);

        assertEquals(Arrays.asList("update:0.016", "render"), game.calls);
    }

    private static final class RecordingGame implements WorldsmithsGame {
        final List<String> calls = new ArrayList<String>();
        final Scene scene = new Scene("recording");

        @Override
        public Scene createScene() {
            calls.add("createScene");
            return scene;
        }

        @Override
        public void create() {
            calls.add("create");
        }

        @Override
        public void update(float delta) {
            calls.add("update:" + delta);
        }

        @Override
        public void render() {
            calls.add("render");
        }
    }
}
