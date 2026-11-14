package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.worldsmiths.core.WorldsmithsGame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopGameListenerTest {

    @Test
    void createForwardsToTheGame() {
        RecordingGame game = new RecordingGame();
        DesktopGameListener listener = new DesktopGameListener(game);

        listener.create();

        assertEquals(Arrays.asList("create"), game.calls);
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
