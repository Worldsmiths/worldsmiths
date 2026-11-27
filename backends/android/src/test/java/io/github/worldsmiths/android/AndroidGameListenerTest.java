package io.github.worldsmiths.android;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.GL20;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.FakeEngineServices;
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
    void createPassesNonNullEngineServicesWithoutCreateScene() {
        RecordingGame game = new RecordingGame();
        FakeEngineServices fake = new FakeEngineServices();
        AndroidGameListener listener = new AndroidGameListener(game, null, fake);

        listener.create();

        assertEquals(Arrays.asList("create"), game.calls);
        assertSame(fake, game.services);
        assertSame(game.sceneA, fake.scenes().current());
        assertNotNull(game.services.input());
        assertNotNull(game.services.clock());
        assertNotNull(game.services.graphics());
        assertNotNull(game.services.audio());
        assertNotNull(game.services.ui());
    }

    @Test
    void updateAndRenderForwardToTheGameWithoutStartingActivity() {
        RecordingGame game = new RecordingGame();
        AndroidGameListener listener = new AndroidGameListener(game);

        listener.updateAndRender(0.016f);

        assertEquals(Arrays.asList("update:0.016", "render"), game.calls);
    }

    @Test
    void updateAndRenderDrawsCurrentSceneAfterGameRender() {
        RecordingGame game = new RecordingGame();
        Sprite sprite = new Sprite("head.png");
        sprite.r = 0.1f;
        sprite.g = 0.2f;
        sprite.b = 0.3f;
        sprite.a = 1f;
        game.sceneA.add(new Node("head").add(new Transform2D(1f, 2f)).add(sprite));

        SceneShapeRenderer recorder = SceneShapeRenderer.recordingOnly();
        FakeEngineServices fake = new FakeEngineServices();
        AndroidGameListener listener = new AndroidGameListener(game, recorder, fake);
        listener.create();

        listener.updateAndRender(0.016f);

        assertEquals(Arrays.asList("create", "update:0.016", "render"), game.calls);
        assertSame(fake, game.services);
        List<ShapeDrawCommand> draws = recorder.lastDrawCommands();
        assertEquals(1, draws.size());
        assertEquals("head.png", draws.get(0).textureKey);
        assertEquals(1f * SceneWalk.CELL_SIZE, draws.get(0).x, 0.001f);
        assertEquals(2f * SceneWalk.CELL_SIZE, draws.get(0).y, 0.001f);
        assertEquals(0.1f, draws.get(0).r, 0.001f);
    }

    @Test
    void afterSceneHopDrawIncludesOnlyCurrentMarkers() {
        RecordingGame game = new RecordingGame();
        game.sceneA.add(
                new Node("a").add(new Transform2D(0f, 0f)).add(new Sprite("marker-a.png")));
        game.sceneB.add(
                new Node("b").add(new Transform2D(1f, 0f)).add(new Sprite("marker-b.png")));

        SceneShapeRenderer recorder = SceneShapeRenderer.recordingOnly();
        FakeEngineServices fake = new FakeEngineServices();
        AndroidGameListener listener = new AndroidGameListener(game, recorder, fake);
        listener.create();

        listener.updateAndRender(0.016f);
        assertEquals(1, recorder.lastDrawCommands().size());
        assertEquals("marker-a.png", recorder.lastDrawCommands().get(0).textureKey);

        game.hopToB();

        listener.updateAndRender(0.016f);

        List<ShapeDrawCommand> draws = recorder.lastDrawCommands();
        assertEquals(1, draws.size());
        assertEquals("marker-b.png", draws.get(0).textureKey);
        assertFalse(
                draws.stream().anyMatch(d -> "marker-a.png".equals(d.textureKey)),
                "draw must not include previous scene markers");
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
        final Scene sceneA = new Scene("scene-a");
        final Scene sceneB = new Scene("scene-b");
        EngineServices services;

        @Override
        public void create(EngineServices services) {
            this.services = services;
            services.scenes().switchTo(sceneA);
            calls.add("create");
        }

        void hopToB() {
            services.scenes().switchTo(sceneB);
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
