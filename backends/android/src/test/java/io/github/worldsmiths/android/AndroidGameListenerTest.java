package io.github.worldsmiths.android;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.GL20;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneWalk;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import io.github.worldsmiths.core.WorldsmithsGame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AndroidGameListenerTest {

    @Test
    void createCallsCreateSceneThenCreateAndRetainsReturnedScene() {
        RecordingGame game = new RecordingGame();
        AndroidGameListener listener = new AndroidGameListener(game);

        listener.create();

        assertEquals(Arrays.asList("createScene", "create"), game.calls);
        assertSame(game.scene, listener.retainedScene());
    }

    @Test
    void updateAndRenderForwardToTheGameWithoutStartingActivity() {
        RecordingGame game = new RecordingGame();
        AndroidGameListener listener = new AndroidGameListener(game);

        listener.updateAndRender(0.016f);

        assertEquals(Arrays.asList("update:0.016", "render"), game.calls);
    }

    @Test
    void updateAndRenderDrawsRetainedSceneAfterGameRender() {
        RecordingGame game = new RecordingGame();
        Sprite sprite = new Sprite("head.png");
        sprite.r = 0.1f;
        sprite.g = 0.2f;
        sprite.b = 0.3f;
        sprite.a = 1f;
        game.scene.add(new Node("head").add(new Transform2D(1f, 2f)).add(sprite));

        SceneShapeRenderer recorder = SceneShapeRenderer.recordingOnly();
        AndroidGameListener listener = new AndroidGameListener(game, recorder);
        listener.create();

        listener.updateAndRender(0.016f);

        assertEquals(
                Arrays.asList("createScene", "create", "update:0.016", "render"), game.calls);
        List<ShapeDrawCommand> draws = recorder.lastDrawCommands();
        assertEquals(1, draws.size());
        assertEquals(1f * SceneWalk.CELL_SIZE, draws.get(0).x, 0.001f);
        assertEquals(2f * SceneWalk.CELL_SIZE, draws.get(0).y, 0.001f);
        assertEquals(0.1f, draws.get(0).r, 0.001f);
    }

    @Test
    void disposeInvokesGameDisposeThenDisposesSceneShapeRenderer() {
        RecordingGame game = new RecordingGame();
        SceneShapeRenderer recorder = SceneShapeRenderer.recordingOnly();
        AndroidGameListener listener = new AndroidGameListener(game, recorder);

        listener.dispose();

        assertEquals(Arrays.asList("dispose"), game.calls);
        assertTrue(recorder.isDisposed());
    }

    @Test
    void frameClearMaskIncludesColorAndDepth() {
        assertEquals(
                GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT,
                AndroidGameListener.FRAME_CLEAR_MASK);
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
        public void create(io.github.worldsmiths.core.EngineServices services) {
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

        @Override
        public void dispose() {
            calls.add("dispose");
        }
    }
}
