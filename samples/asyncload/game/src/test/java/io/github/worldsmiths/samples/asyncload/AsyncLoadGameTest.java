package io.github.worldsmiths.samples.asyncload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.FakeLog;
import io.github.worldsmiths.core.LoadState;
import org.junit.jupiter.api.Test;

/**
 * Headless async load path: FakeAssets seed + pump to COMPLETE, FakeLog ≥1 load-related emit.
 * No display or GDX in game sources.
 */
class AsyncLoadGameTest {

    private static final String HEAVY_PATH = "textures/heavy.png";

    @Test
    void createThenPumpedUpdateCompletesAndEmitsLoadLog() {
        FakeEngineServices services = new FakeEngineServices();
        services.assets().putTexture(HEAVY_PATH);

        AsyncLoadGame game = new AsyncLoadGame();
        game.createScene();
        game.create(services);

        assertEquals(LoadState.QUEUED, game.loadHandle().state());

        services.assets().pump();
        game.update(0f);

        assertEquals(LoadState.COMPLETE, game.loadHandle().state());

        int loadRelated = 0;
        for (FakeLog.Command cmd : services.log().commands()) {
            if (isLoadRelated(cmd)) {
                loadRelated += 1;
            }
        }
        assertTrue(
                loadRelated >= 1,
                "expected ≥1 FakeLog info/warn/error about the load, got "
                        + services.log().commands().size()
                        + " total commands");
    }

    private static boolean isLoadRelated(FakeLog.Command cmd) {
        if (cmd.level != FakeLog.Command.Level.INFO
                && cmd.level != FakeLog.Command.Level.WARN
                && cmd.level != FakeLog.Command.Level.ERROR) {
            return false;
        }
        String haystack = ((cmd.tag == null ? "" : cmd.tag) + " " + cmd.message).toLowerCase();
        return haystack.contains("heavy")
                || haystack.contains("load")
                || haystack.contains("texture")
                || haystack.contains("async")
                || haystack.contains("complete")
                || haystack.contains("fail");
    }
}
